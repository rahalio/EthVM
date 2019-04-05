package io.enkrypt.kafka.streams.processors

import io.enkrypt.avro.capture.TraceCallActionRecord
import io.enkrypt.avro.capture.TraceCreateActionRecord
import io.enkrypt.avro.capture.TraceDestroyActionRecord
import io.enkrypt.avro.processing.BlockHeaderMetricsRecord
import io.enkrypt.avro.processing.BlockTimestampRecord
import io.enkrypt.avro.processing.BlockTransactionFeeMetricsRecord
import io.enkrypt.avro.processing.BlockTransactionMetricsRecord
import io.enkrypt.avro.processing.BlockTransactionTraceMetricsRecord
import io.enkrypt.common.extensions.getBalanceBI
import io.enkrypt.common.extensions.getGasBI
import io.enkrypt.common.extensions.getGasPriceBI
import io.enkrypt.common.extensions.getTransactionFeeBI
import io.enkrypt.common.extensions.getValueBI
import io.enkrypt.common.extensions.setAvgGasLimitBI
import io.enkrypt.common.extensions.setAvgGasPriceBI
import io.enkrypt.common.extensions.setAvgTxFeesBI
import io.enkrypt.common.extensions.setTotalGasPriceBI
import io.enkrypt.common.extensions.setTotalTxFeesBI
import io.enkrypt.kafka.streams.Serdes
import io.enkrypt.kafka.streams.config.Topics.BlockHeaderMetrics
import io.enkrypt.kafka.streams.config.Topics.BlockTimestamp
import io.enkrypt.kafka.streams.config.Topics.BlockTransactionFeeMetrics
import io.enkrypt.kafka.streams.config.Topics.BlockTransactionMetrics
import io.enkrypt.kafka.streams.config.Topics.BlockTransactionTraceMetrics
import io.enkrypt.kafka.streams.config.Topics.CanonicalBlockHeader
import io.enkrypt.kafka.streams.config.Topics.CanonicalTraces
import io.enkrypt.kafka.streams.config.Topics.CanonicalTransactionFees
import io.enkrypt.kafka.streams.config.Topics.CanonicalTransactions
import io.enkrypt.kafka.streams.transformers.BlockTimeTransformer
import io.enkrypt.kafka.streams.utils.toTopic
import mu.KLogger
import mu.KotlinLogging
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.JoinWindows
import org.apache.kafka.streams.kstream.Joined
import org.apache.kafka.streams.kstream.TransformerSupplier
import java.math.BigInteger
import java.time.Duration
import java.util.Properties

class BlockMetricsProcessor : AbstractKafkaProcessor() {

  override val id: String = "block-metrics-processor"

  override val kafkaProps: Properties = Properties()
    .apply {
      putAll(baseKafkaProps.toMap())
      put(StreamsConfig.APPLICATION_ID_CONFIG, id)
      put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1)
    }

  override val logger: KLogger = KotlinLogging.logger {}

  override fun buildTopology(): Topology {

    // Create stream builder
    val builder = StreamsBuilder().apply {
      addStateStore(BlockTimeTransformer.blockTimesStore(appConfig.unitTesting))
    }

    val canonicalBlock = CanonicalBlockHeader.stream(builder)

    canonicalBlock
      .mapValues { _, v ->
        BlockTimestampRecord.newBuilder()
          .setTimestamp(v.getTimestamp())
          .build()
      }.toTopic(BlockTimestamp)

    //

    canonicalBlock.transform(
      TransformerSupplier { BlockTimeTransformer(appConfig.unitTesting) },
      *BlockTimeTransformer.STORE_NAMES
    )
      .mapValues { header ->
        BlockHeaderMetricsRecord.newBuilder()
          .setBlockTime(header.getBlockTime())
          .setNumUncles(header.getUncles().size)
          .setDifficulty(header.getDifficulty())
          .setTotalDifficulty(header.getTotalDifficulty())
          .setTimestamp(header.getTimestamp())
          .build()
      }.toTopic(BlockHeaderMetrics)

    val blockTimestamp = BlockTimestamp.stream(builder)

    CanonicalTraces.stream(builder)
      .mapValues { traceList ->

        var successful = 0
        var failed = 0
        var total = 0
        var internalTxs = 0

        traceList.getTraces()
          .filter { it.getTransactionHash() != null } // rewards have no tx hash, only a block hash
          .groupBy { it.getTransactionHash() }
          .forEach { (_, traces) ->

            traces.forEach { trace ->

              val action = trace.getAction()

              when (action) {
                is TraceCallActionRecord -> {

                  if (trace.getTraceAddress().isEmpty()) {

                    // high level parent call is used to determine tx success
                    when (trace.getError()) {
                      null -> successful += 1
                      "" -> successful += 1
                      else -> failed += 1
                    }

                    total += 1
                  }

                  if (action.getValueBI()!! > BigInteger.ZERO) {
                    internalTxs += 1
                  }
                }
                is TraceCreateActionRecord -> {
                  if (action.getValueBI()!! > BigInteger.ZERO) {
                    internalTxs += 1
                  }
                }
                is TraceDestroyActionRecord -> {
                  if (action.getBalanceBI()!! > BigInteger.ZERO) {
                    internalTxs += 1
                  }
                }
                else -> {
                }
              }
            }
          }

        BlockTransactionTraceMetricsRecord.newBuilder()
          .setNumSuccessfulTxs(successful)
          .setNumFailedTxs(failed)
          .setTotalTxs(total)
          .setNumInternalTxs(internalTxs)
          .build()
      }
      .join(
        blockTimestamp,
        { left, right ->
          BlockTransactionTraceMetricsRecord.newBuilder(left)
            .setTimestamp(right.getTimestamp())
            .build()
        },
        JoinWindows.of(Duration.ofHours(2)),
        Joined.with(Serdes.CanonicalKey(), Serdes.BlockTransactionTraceMetrics(), Serdes.BlockTimestamp())
      )
      .toTopic(BlockTransactionTraceMetrics)

    CanonicalTransactions.stream(builder)
      .mapValues { transactionsList ->

        val transactions = transactionsList.getTransactions()

        var totalGasPrice = BigInteger.ZERO
        var totalGasLimit = BigInteger.ZERO

        transactions.forEach { tx ->
          totalGasLimit += tx.getGasBI()!!
          totalGasPrice += tx.getGasPriceBI()!!
        }

        val txCount = transactions.size.toBigInteger()

        val (avgGasPrice, avgGasLimit) =
          when (txCount) {
            BigInteger.ZERO -> listOf(BigInteger.ZERO, BigInteger.ZERO)
            else -> listOf(
              totalGasPrice / txCount,
              totalGasLimit / txCount
            )
          }

        BlockTransactionMetricsRecord.newBuilder()
          .setTotalGasPriceBI(totalGasPrice)
          .setAvgGasPriceBI(avgGasPrice)
          .setAvgGasLimitBI(avgGasLimit)
          .build()
      }
      .join(
        blockTimestamp,
        { left, right ->
          BlockTransactionMetricsRecord.newBuilder(left)
            .setTimestamp(right.getTimestamp())
            .build()
        },
        JoinWindows.of(Duration.ofHours(2)),
        Joined.with(Serdes.CanonicalKey(), Serdes.BlockTransactionMetrics(), Serdes.BlockTimestamp())
      )
      .toTopic(BlockTransactionMetrics)

    CanonicalTransactionFees.stream(builder)
      .mapValues { txFeeList ->

        val transactionFees = txFeeList.getTransactionFees()

        val totalTxFees = transactionFees.fold(BigInteger.ZERO) { memo, next ->
          memo + next.getTransactionFeeBI()!!
        }

        val count = transactionFees.size.toBigInteger()

        val avgTxFees = when (count) {
          BigInteger.ZERO -> BigInteger.ZERO
          else -> totalTxFees / count
        }

        BlockTransactionFeeMetricsRecord.newBuilder()
          .setTotalTxFeesBI(totalTxFees)
          .setAvgTxFeesBI(avgTxFees)
          .build()
      }
      .join(
        blockTimestamp,
        { left, right ->
          BlockTransactionFeeMetricsRecord.newBuilder(left)
            .setTimestamp(right.getTimestamp())
            .build()
        },
        JoinWindows.of(Duration.ofHours(2)),
        Joined.with(Serdes.CanonicalKey(), Serdes.BlockTransactionFeeMetrics(), Serdes.BlockTimestamp())
      )
      .toTopic(BlockTransactionFeeMetrics)

    return builder.build()
  }
}
