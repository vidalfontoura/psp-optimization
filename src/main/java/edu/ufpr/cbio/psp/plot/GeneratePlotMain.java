package edu.ufpr.cbio.psp.plot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.NamedPlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.ImageTerminal;

public class GeneratePlotMain {

    public static void main(String[] args) throws IOException {

        plotCasSPEA2();
    }

    public static void plotCasSPEA2() throws IOException {

        List<PlotPoint> plotPoints = new ArrayList<>();

        // /////////////////////////////////////////////////////////////////////////////////////////////////
        PlotStyle plotStyleSpea2 = new PlotStyle();
        plotStyleSpea2.setStyle(Style.POINTS);
        plotStyleSpea2.setLineWidth(2);
        plotStyleSpea2.setLineType(NamedPlotColor.RED);

        PlotPoint plotPointSpea = new PlotPoint();
        plotPointSpea.setDataPath("results-tuning-bkp" + File.separator
            + "P6HPH2P5H3PH5PH2P4H2P2H2PH5PH10PH2PH7P11H7P2HPH3P6HPH2" + File.separator + "SPEA2" + File.separator
            + "C7" + File.separator + "FUN.txt");
        plotPointSpea.setTitlePoint("SPEA2");
        plotPointSpea.setPlotStyle(plotStyleSpea2);

        plotPoints.add(plotPointSpea);

        // //
        // /////////////////////////////////////////////////////////////////////////////////////////////////
        // PlotStyle plotStyleIBEA = new PlotStyle();
        // plotStyleIBEA.setStyle(Style.POINTS);
        // plotStyleIBEA.setLineWidth(2);
        // plotStyleIBEA.setLineType(NamedPlotColor.GREEN);
        //
        // PlotPoint plotPointIBEA = new PlotPoint();
        // plotPointIBEA.setDataPath("experiments" + File.separator +
        // "result-test" + File.separator + "CAS"
        // + File.separator + "IBEA" + File.separator + "FUN.txt");
        // plotPointIBEA.setTitlePoint("IBEA");
        // plotPointIBEA.setPlotStyle(plotStyleIBEA);
        //
        // plotPoints.add(plotPointIBEA);
        //
        // //
        // //
        // ///////////////////////////////////////////////////////////////////////////////////////////////
        // PlotStyle plotStyleNSGAII = new PlotStyle();
        // plotStyleNSGAII.setStyle(Style.POINTS);
        // plotStyleNSGAII.setLineType(NamedPlotColor.BLUE);
        //
        // PlotPoint plotPointNSGAII = new PlotPoint();
        // plotPointNSGAII.setDataPath("experiments" + File.separator +
        // "result-test" + File.separator + "CAS"
        // + File.separator + "NSGAII" + File.separator + "FUN.txt");
        // plotPointNSGAII.setTitlePoint("NSGAII");
        // plotPointNSGAII.setPlotStyle(plotStyleNSGAII);
        //
        // plotPoints.add(plotPointNSGAII);

        // /////////////////////////////////////////////////////////////////////////////////////////////////

        plot2lines("Energia", "Distancia Euclidiana", 0.0, 50.0, 0.0, 20, plotPoints, "Fronteira de Pareto",
            "spea2.png", "png");

    }

    private static void plot2lines(String labelX, String labelY, double rangeStartX, double rangeEndX,
                                   double rangeStartY, double rangeEndY, List<PlotPoint> plotPoints, String titleGraph,
                                   String pathSave, String fileExtension) throws IOException {

        System.out.println("Salvando o arquivo em: " + System.getProperty("user.dir") + File.separator + pathSave);
        ImageTerminal png = new ImageTerminal();
        File file = new File(System.getProperty("user.dir") + File.separator + pathSave);
        try {
            file.createNewFile();
            png.processOutput(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            System.err.print(ex);
        } catch (IOException ex) {
            System.err.print(ex);
        }

        JavaPlot p = new JavaPlot();
        p.setTerminal(png);
        // p.set("size", "square");

        p.getAxis("x").setLabel(labelX);
        p.getAxis("y").setLabel(labelY);
        p.getAxis("x").setBoundaries(rangeStartX, rangeEndX);
        p.getAxis("y").setBoundaries(rangeStartY, rangeEndY);

        for (PlotPoint plotPoint : plotPoints) {
            // Read data file A
            InstanceReader reader = new InstanceReader(plotPoint.getDataPath());
            reader.open();
            double[][] values = reader.readDoubleMatrix(" ", true);
            reader.close();

            DataSetPlot dataSet = new DataSetPlot(values);
            dataSet.setPlotStyle(plotPoint.getPlotStyle());
            dataSet.setTitle(plotPoint.getTitlePoint());

            p.addPlot(dataSet);
        }

        p.setTitle(titleGraph);
        // p.newGraph3D();
        p.plot();

        try {
            ImageIO.write(png.getImage(), fileExtension, file);
            System.out.println("Arquivo salvo com sucesso.");
        } catch (IOException ex) {
            System.err.print(ex);
        }
    }
}
