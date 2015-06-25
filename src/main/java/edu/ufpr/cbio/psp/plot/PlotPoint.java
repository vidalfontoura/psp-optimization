package edu.ufpr.cbio.psp.plot;

import com.panayotis.gnuplot.style.PlotStyle;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */

public class PlotPoint {

    private String titlePoint;
    private String dataPath;
    private PlotStyle plotStyle;

    public PlotPoint() {

    }

    public PlotPoint(String titlePoint, String dataPath, PlotStyle plotStyle) {

        this.titlePoint = titlePoint;
        this.dataPath = dataPath;
        this.plotStyle = plotStyle;
    }

    /**
     * @return the titlePoint
     */
    public String getTitlePoint() {

        return titlePoint;
    }

    /**
     * @param titlePoint the titlePoint to set
     */
    public void setTitlePoint(String titlePoint) {

        this.titlePoint = titlePoint;
    }

    /**
     * @return the dataPath
     */
    public String getDataPath() {

        return dataPath;
    }

    /**
     * @param dataPath the dataPath to set
     */
    public void setDataPath(String dataPath) {

        this.dataPath = dataPath;
    }

    /**
     * @return the plotStyle
     */
    public PlotStyle getPlotStyle() {

        return plotStyle;
    }

    /**
     * @param plotStyle the plotStyle to set
     */
    public void setPlotStyle(PlotStyle plotStyle) {

        this.plotStyle = plotStyle;
    }
}
