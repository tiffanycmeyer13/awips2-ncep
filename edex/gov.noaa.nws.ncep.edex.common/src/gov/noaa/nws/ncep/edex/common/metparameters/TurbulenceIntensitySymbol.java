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
 * Maps to the GEMPAK parameter TBSY
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize


public class TurbulenceIntensitySymbol extends AbstractMetParameter<Dimensionless> 
        implements ISerializableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8266804270282509278L;

	public TurbulenceIntensitySymbol() {
		super(AbstractUnit.ONE);
		setValueIsString();
	}

}
