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
 * Maps to the parameter TS24
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize

 public class UncondProbOfTstorms24hr extends AbstractMetParameter<Dimensionless>
        implements ISerializableObject {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -941540151994722478L;

	public UncondProbOfTstorms24hr() throws Exception {
		 super(AbstractUnit.ONE);
	}
	 
 }