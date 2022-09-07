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
 * Maps to the GEMPAK parameter TPOT
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize


public class TurbulenceTypeSymbol extends AbstractMetParameter<Dimensionless> 
        implements ISerializableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6287889028169403542L;

	public TurbulenceTypeSymbol() {
		super(AbstractUnit.ONE);
		setValueIsString();
	}

}