import Vue from 'vue'

import Header from '@/components/Header'
Vue.component('Header', Header)

import Footer from '@/components/Footer'
Vue.component('Footer', Footer)

import HpTxContainer from '@/components/HomePage/TxContainer'
Vue.component('hp-tx-container', HpTxContainer)

import HpBlockContainer from '@/components/HomePage/blockContainer'
Vue.component('hp-block-container', HpBlockContainer)

import LatestBlocks from '@/components/LatestBlocks/LatestBlocks'
Vue.component('LatestBlocks', LatestBlocks)

import Block from '@/components/Block/Block'
Vue.component('Block', Block)

import LatestTransaction from '@/components/LatestTransaction/LatestTransaction'
Vue.component('LatestTransaction', LatestTransaction)

import TxPagination from '@/components/LatestTransaction/TxPagination'
Vue.component('TxPagination', TxPagination)

import IndividualTransaction from '@/components/IndividualTransaction/IndividualTransaction'
Vue.component('IndividualTransaction', IndividualTransaction)

import SingleAccount from '@/components/SingleAccount/SingleAccount'
Vue.component('SingleAccount', SingleAccount)

import TopNav from "@/components/TopNav"
Vue.component('topnav', TopNav)

import * as Icon from 'vue-awesome'
import 'vue-awesome/icons'
Vue.component('icon', Icon)


/* NewHome ############################################################################## */


    /* Frames ---------------------- */
    import FramesMainFrame from "@/components/NewHome/Frames/FramesMainFrame"
    Vue.component('FramesMainFrame', FramesMainFrame)

    import FramesHome from "@/components/NewHome/Frames/FramesHome"
    Vue.component('FramesHome', FramesHome)

    import FramesAbout from "@/components/NewHome/Frames/FramesAbout"
    Vue.component('FramesAbout', FramesAbout)

    import FramesFAQ from "@/components/NewHome/Frames/FramesFAQ"
    Vue.component('FramesFAQ', FramesFAQ)

    import FramesContact from "@/components/NewHome/Frames/FramesContact"
    Vue.component('FramesContact', FramesContact)

    import FrameIndividualTransaction from "@/components/NewHome/Frames/FrameIndividualTransaction"
    Vue.component('FrameIndividualTransaction', FrameIndividualTransaction)




    /* Menu ---------------------- */
    import MenusTop from "@/components/NewHome/Sections/Menus/MenusTop"
    Vue.component('MenusTop', MenusTop)

    import MenusSide from "@/components/NewHome/Sections/Menus/MenusSide"
    Vue.component('MenusSide', MenusSide)



    /* ShortData ---------------------- */
    import ShortDataLastBlock from "@/components/NewHome/Sections/ShortData/ShortDataLastBlock"
    Vue.component('ShortDataLastBlock', ShortDataLastBlock)
    
    import ShortDataTimeSinceLastBlock from "@/components/NewHome/Sections/ShortData/ShortDataTimeSinceLastBlock"
    Vue.component('ShortDataTimeSinceLastBlock', ShortDataTimeSinceLastBlock)

    import ShortDataHashRate from "@/components/NewHome/Sections/ShortData/ShortDataHashRate"
    Vue.component('ShortDataHashRate', ShortDataHashRate)

    import ShortDataDifficulty from "@/components/NewHome/Sections/ShortData/ShortDataDifficulty"
    Vue.component('ShortDataDifficulty', ShortDataDifficulty)



    /* Graphs ---------------------- */
    import GraphsRealChart from "@/components/NewHome/Sections/Graphs/GraphsRealChart"
    Vue.component('GraphsRealChart', GraphsRealChart)

    import GraphsBarChart from "@/components/NewHome/Sections/Graphs/GraphsBarChart"
    Vue.component('GraphsBarChart', GraphsBarChart)

    import GraphsLineChart from "@/components/NewHome/Sections/Graphs/GraphsLineChart"
    Vue.component('GraphsLineChart', GraphsLineChart)




    /* Tables ---------------------- */
    import TablesLastTransactions from "@/components/NewHome/Sections/Tables/TablesLastTransactions"
    Vue.component('TablesLastTransactions', TablesLastTransactions)

    import TablesLatestBlocks from "@/components/NewHome/Sections/Tables/TablesLatestBlocks"
    Vue.component('TablesLatestBlocks', TablesLatestBlocks)




    /* Footers ---------------------- */
    import FootersBottom from "@/components/NewHome/Sections/Footers/FootersBottom"
    Vue.component('FootersBottom', FootersBottom)




    /* Info  ---------------------- */
    import FAQ from "@/components/NewHome/Sections/Info/FAQ"
    Vue.component('FAQ', FAQ)
    
    import About from "@/components/NewHome/Sections/Info/About"
    Vue.component('About', About)
    
    import Contact from "@/components/NewHome/Sections/Info/Contact"
    Vue.component('Contact', Contact)



    /* SinglePages  ---------------------- */
    import SinglePagesIndividualTransaction from "@/components/NewHome/Sections/SinglePages/SinglePagesIndividualTransaction"
    Vue.component('SinglePagesIndividualTransaction', SinglePagesIndividualTransaction)
    


