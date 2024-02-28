/**
 * 
 */
package gov.noaa.nws.ncep.edex.common.metparameters;

import javax.measure.quantity.Dimensionless;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

import tech.units.indriya.AbstractUnit;

/**
 * Maps to the GEMPAK parameter PP1C
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class Clim12HrPOP
        extends AbstractMetParameter<Dimensionless>
        implements ISerializableObject {
    
    private static final long serialVersionUID = 5186883329582900913L;

    public Clim12HrPOP() {
        super(AbstractUnit.ONE);
    }

    @Override
    public String getParameterDescription() {
        return "Climatological 12 Hour Probability of Precipitation.";
    }

}