package gov.noaa.nws.ncep.viz.rsc.ncgrid.contours;

import java.util.Arrays;

import javax.measure.UnitConverter;

import tech.units.indriya.function.AbstractConverter;

/**
 * ContourUnitConverter
 * 
 * provides process to convert intervals from ContourSupport, when rendering
 * image contour fills.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 11/16/2015   R13016     mkean       Initial creation.
 * Apr 29, 2019 7596       lsingh      Updated units framework to JSR-363.
 * Aug 05, 2022 8905       lsingh      Updated units framework to 2.0.2.
 *                                     Renamed methods, and overrided additional methods.
 * 
 * </pre>
 * 
 * @author mkean
 */

public class ContourUnitConverter extends AbstractConverter {

    private static final long serialVersionUID = 1L;

    private double[] xVals;

    private double[] yVals;

    public ContourUnitConverter(double[] xVals, double[] yVals) {
        this.xVals = xVals;
        this.yVals = yVals;
    }

    @Override
    public Number convertWhenNotIdentity(Number x) {
        if (Double.isNaN(x.doubleValue())) {
            return Double.NaN;
        }
        if (Double.isInfinite(x.doubleValue())) {
            return x;
        }
        double increment = (yVals[yVals.length - 1] - yVals[0])
                / ((double) yVals.length - 1);
        int index = (int) Math.round((x.doubleValue() - yVals[0]) / increment);

        return yVals[index];
    }

    @Override
    public AbstractConverter inverseWhenNotIdentity() {
        return new ContourUnitConverter(yVals, xVals);
    }

    @Override
    public boolean isLinear() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Float.floatToIntBits((float)convert(1.0));
        result = prime * result + Arrays.hashCode(xVals);
        result = prime * result + Arrays.hashCode(yVals);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        final ContourUnitConverter other = (ContourUnitConverter) obj;
        if (!Arrays.equals(xVals, other.xVals))
            return false;
        if (!Arrays.equals(yVals, other.yVals))
            return false;
        return true;
    }

    @Override
    public boolean isIdentity() {
        return false;
    }

    @Override
    public int compareTo(UnitConverter o) {
     // This method hasn't been implemented yet since it's unused
        return 0;
    }

    @Override
    protected String transformationLiteral() {
     // This method hasn't been implemented yet since it's unused
        return null;
    }

    @Override
    protected boolean canReduceWith(AbstractConverter that) {
     // This method hasn't been implemented yet since it's unused
        return false;
    }

}
