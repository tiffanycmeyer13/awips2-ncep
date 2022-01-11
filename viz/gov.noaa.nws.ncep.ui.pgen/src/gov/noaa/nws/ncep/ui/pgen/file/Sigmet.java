//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2009.11.20 at 10:41:49 AM EST
//

package gov.noaa.nws.ncep.ui.pgen.file;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}Color" maxOccurs="unbounded"/>
 *         &lt;element ref="{}Point" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="pgenType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="fillPattern" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="filled" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="closed" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="smoothFactor" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="sizeScale" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="lineWidth" type="{http://www.w3.org/2001/XMLSchema}float" />
 *       &lt;attribute name="pgenCategory" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="width" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="editableAttrArea" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrIssueOffice" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrStatus" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrSeqNum" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrStartTime" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrEndTime" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrRemarks" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrPhenom" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrPhenom2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrPhenomName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrPhenomLat" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrPhenomLon" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrPhenomPressure" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrPhenomMaxWind" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrFreeText" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrTrend" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrMovement" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrPhenomSpeed" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrPhenomDirection" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrLevel" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrLevelInfo1" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrLevelInfo2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrLevelText1" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrLevelText2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrAltLevel" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrAltLevelInfo1" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrAltLevelInfo2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrAltLevelText1" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrAltLevelText2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrFromLine" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="editableAttrFir" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
/**
 * Element class for sigmet.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * ???          ???         ???         Initial Creation.
 * 04/28/20     77994       ksunil      new fields for TC.
 * May 22, 2020 78000       ksunil      New Tropical Cyclone UI components for Fcst
 * Feb 08, 2021 87538       smanoj      Added FCST Lat/Lon for Tropical Cyclone.
 * Apr 08, 2021 90325       smanoj      CARSAM Backup WMO headers update.
 * Jun 18, 2021 90732       mroos       Added variables for VolAsh altitude level info
 * Jan 10, 2022 99344       smanoj      Added VolcAsh Description coordinate RoundTo Value.
 *
 * </pre>
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "color", "point" })
@XmlRootElement(name = "Sigmet")
public class Sigmet {

    @XmlElement(name = "Color", required = true)
    protected List<Color> color;

    @XmlElement(name = "Point", required = true)
    protected List<Point> point;

    @XmlAttribute
    protected String pgenType;

    @XmlAttribute
    protected String fillPattern;

    @XmlAttribute
    protected Boolean filled;

    @XmlAttribute
    protected Boolean closed;

    @XmlAttribute
    protected Integer smoothFactor;

    @XmlAttribute
    protected Double sizeScale;

    @XmlAttribute
    protected Float lineWidth;

    @XmlAttribute
    protected String pgenCategory;

    @XmlAttribute
    protected String type;

    @XmlAttribute
    protected Double width;

    @XmlAttribute
    protected String editableAttrArea;

    @XmlAttribute
    protected String editableAttrIssueOffice;

    @XmlAttribute
    protected String editableAttrStatus;

    @XmlAttribute
    protected String editableAttrId;

    @XmlAttribute
    protected String editableAttrSeqNum;

    @XmlAttribute
    protected String editableAttrStartTime;

    @XmlAttribute
    protected String editableAttrEndTime;

    @XmlAttribute
    protected String editableAttrRemarks;

    @XmlAttribute
    protected String editableAttrPhenom;

    @XmlAttribute
    protected String editableAttrPhenom2;

    @XmlAttribute
    protected String editableAttrPhenomName;

    @XmlAttribute
    protected String editableAttrPhenomLat;

    @XmlAttribute
    protected String editableAttrPhenomLon;

    @XmlAttribute
    protected String editableAttrPhenomPressure;

    @XmlAttribute
    protected String editableAttrPhenomMaxWind;

    @XmlAttribute
    protected String editableAttrFreeText;

    @XmlAttribute
    protected String editableAttrTrend;

    @XmlAttribute
    protected String editableAttrMovement;

    @XmlAttribute
    protected String editableAttrPhenomSpeed;

    @XmlAttribute
    protected String editableAttrPhenomDirection;

    @XmlAttribute
    protected String editableAttrLevel;

    @XmlAttribute
    protected String editableAttrLevelInfo1;

    @XmlAttribute
    protected String editableAttrLevelInfo2;

    @XmlAttribute
    protected String editableAttrLevelText1;

    @XmlAttribute
    protected String editableAttrLevelText;

    @XmlAttribute
    protected String editableAttrLevelText2;

    @XmlAttribute
    protected String editableAttrAltLevel;

    @XmlAttribute
    protected String editableAttrAltLevelInfo1;

    @XmlAttribute
    protected String editableAttrAltLevelInfo2;

    @XmlAttribute
    protected String editableAttrAltLevelText1;

    @XmlAttribute
    protected String editableAttrAltLevelText;

    @XmlAttribute
    protected String editableAttrAltLevelText2;

    @XmlAttribute
    protected String editableAttrFromLine;

    @XmlAttribute
    protected String editableAttrFir;

    @XmlAttribute
    protected String editableAttrCarSamBackupMode;

    @XmlAttribute
    protected String editableAttrFcstAvail;

    @XmlAttribute
    protected String editableAttrFcstTime;

    @XmlAttribute
    protected String editableAttrFcstCntr;

    @XmlAttribute
    protected String editableAttrFcstPhenomLat;

    @XmlAttribute
    protected String editableAttrFcstPhenomLon;

    @XmlAttribute
    protected String editableAttrFcstVADesc;

    @XmlAttribute
    protected String editableAttrFcstVADescRoundToVal;

    @XmlAttribute
    protected String editableAttrRALSelection;

    @XmlAttribute
    protected String editableAttrAltitudeSelection;

    public String getEditableAttrAltitudeSelection() {
        return editableAttrAltitudeSelection;
    }

    public void setEditableAttrAltitudeSelection(
            String editableAttrAltitudeSelection) {
        this.editableAttrAltitudeSelection = editableAttrAltitudeSelection;
    }

    public String getEditableAttrAltLevelText() {
        return editableAttrAltLevelText;
    }

    public void setEditableAttrAltLevelText(String editableAltLevelText1) {
        this.editableAttrAltLevelText = editableAltLevelText1;
    }

    public String getEditableAttrRALSelection() {
        return editableAttrRALSelection;
    }

    public void setEditableAttrRALSelection(String editableRALSelection) {
        this.editableAttrRALSelection = editableRALSelection;
    }

    public String getEditableAttrFcstVADesc() {
        return editableAttrFcstVADesc;
    }

    public void setEditableAttrFcstVADesc(String editableAttrFcstVADesc) {
        this.editableAttrFcstVADesc = editableAttrFcstVADesc;
    }

    public String getEditableAttrFcstVADescRoundToVal() {
        return editableAttrFcstVADescRoundToVal;
    }

    public void setEditableAttrFcstVADescRoundToVal(
            String editableAttrFcstVADescRoundToVal) {
        this.editableAttrFcstVADescRoundToVal = editableAttrFcstVADescRoundToVal;
    }

    public String getEditableAttrFcstAvail() {
        return editableAttrFcstAvail;
    }

    public void setEditableAttrFcstAvail(String editableAttrFcstAvail) {
        this.editableAttrFcstAvail = editableAttrFcstAvail;
    }

    public String getEditableAttrFcstTime() {
        return editableAttrFcstTime;
    }

    public void setEditableAttrFcstTime(String editableAttrFcstTime) {
        this.editableAttrFcstTime = editableAttrFcstTime;
    }

    public String getEditableAttrFcstCntr() {
        return editableAttrFcstCntr;
    }

    public void setEditableAttrFcstCntr(String editableAttrFcstCntr) {
        this.editableAttrFcstCntr = editableAttrFcstCntr;
    }

    public String getEditableAttrFcstPhenomLat() {
        return editableAttrFcstPhenomLat;
    }

    public void setEditableAttrFcstPhenomLat(String editableAttrFcstPhenomLat) {
        this.editableAttrFcstPhenomLat = editableAttrFcstPhenomLat;
    }

    public String getEditableAttrFcstPhenomLon() {
        return editableAttrFcstPhenomLon;
    }

    public void setEditableAttrFcstPhenomLon(String editableAttrFcstPhenomLon) {
        this.editableAttrFcstPhenomLon = editableAttrFcstPhenomLon;
    }

    /**
     * Gets the value of the color property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the color property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getColor().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Color }
     *
     *
     */
    public List<Color> getColor() {
        if (color == null) {
            color = new ArrayList<>();
        }
        return this.color;
    }

    /**
     * Gets the value of the point property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the point property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getPoint().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Point }
     *
     *
     */
    public List<Point> getPoint() {
        if (point == null) {
            point = new ArrayList<>();
        }
        return this.point;
    }

    /**
     * Gets the value of the pgenType property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getPgenType() {
        return pgenType;
    }

    /**
     * Sets the value of the pgenType property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setPgenType(String value) {
        this.pgenType = value;
    }

    /**
     * Gets the value of the fillPattern property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getFillPattern() {
        return fillPattern;
    }

    /**
     * Sets the value of the fillPattern property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setFillPattern(String value) {
        this.fillPattern = value;
    }

    /**
     * Gets the value of the filled property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public Boolean isFilled() {
        return filled;
    }

    /**
     * Sets the value of the filled property.
     *
     * @param value
     *            allowed object is {@link Boolean }
     *
     */
    public void setFilled(Boolean value) {
        this.filled = value;
    }

    /**
     * Gets the value of the closed property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public Boolean isClosed() {
        return closed;
    }

    /**
     * Sets the value of the closed property.
     *
     * @param value
     *            allowed object is {@link Boolean }
     *
     */
    public void setClosed(Boolean value) {
        this.closed = value;
    }

    /**
     * Gets the value of the smoothFactor property.
     *
     * @return possible object is {@link Integer }
     *
     */
    public Integer getSmoothFactor() {
        return smoothFactor;
    }

    /**
     * Sets the value of the smoothFactor property.
     *
     * @param value
     *            allowed object is {@link Integer }
     *
     */
    public void setSmoothFactor(Integer value) {
        this.smoothFactor = value;
    }

    /**
     * Gets the value of the sizeScale property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getSizeScale() {
        return sizeScale;
    }

    /**
     * Sets the value of the sizeScale property.
     *
     * @param value
     *            allowed object is {@link Double }
     *
     */
    public void setSizeScale(Double value) {
        this.sizeScale = value;
    }

    /**
     * Gets the value of the lineWidth property.
     *
     * @return possible object is {@link Float }
     *
     */
    public Float getLineWidth() {
        return lineWidth;
    }

    /**
     * Sets the value of the lineWidth property.
     *
     * @param value
     *            allowed object is {@link Float }
     *
     */
    public void setLineWidth(Float value) {
        this.lineWidth = value;
    }

    /**
     * Gets the value of the pgenCategory property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getPgenCategory() {
        return pgenCategory;
    }

    /**
     * Sets the value of the pgenCategory property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setPgenCategory(String value) {
        this.pgenCategory = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the width property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     *
     * @param value
     *            allowed object is {@link Double }
     *
     */
    public void setWidth(Double value) {
        this.width = value;
    }

    /**
     * Gets the value of the editableAttrArea property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrArea() {
        return editableAttrArea;
    }

    /**
     * Sets the value of the editableAttrArea property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrArea(String value) {
        this.editableAttrArea = value;
    }

    /**
     * Gets the value of the editableAttrIssueOffice property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrIssueOffice() {
        return editableAttrIssueOffice;
    }

    /**
     * Sets the value of the editableAttrIssueOffice property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrIssueOffice(String value) {
        this.editableAttrIssueOffice = value;
    }

    /**
     * Gets the value of the editableAttrStatus property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrStatus() {
        return editableAttrStatus;
    }

    /**
     * Sets the value of the editableAttrStatus property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrStatus(String value) {
        this.editableAttrStatus = value;
    }

    /**
     * Gets the value of the editableAttrId property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrId() {
        return editableAttrId;
    }

    /**
     * Sets the value of the editableAttrId property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrId(String value) {
        this.editableAttrId = value;
    }

    /**
     * Gets the value of the editableAttrSeqNum property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrSeqNum() {
        return editableAttrSeqNum;
    }

    /**
     * Sets the value of the editableAttrSeqNum property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrSeqNum(String value) {
        this.editableAttrSeqNum = value;
    }

    /**
     * Gets the value of the editableAttrStartTime property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrStartTime() {
        return editableAttrStartTime;
    }

    /**
     * Sets the value of the editableAttrStartTime property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrStartTime(String value) {
        this.editableAttrStartTime = value;
    }

    /**
     * Gets the value of the editableAttrEndTime property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrEndTime() {
        return editableAttrEndTime;
    }

    /**
     * Sets the value of the editableAttrEndTime property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrEndTime(String value) {
        this.editableAttrEndTime = value;
    }

    /**
     * Gets the value of the editableAttrRemarks property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrRemarks() {
        return editableAttrRemarks;
    }

    /**
     * Sets the value of the editableAttrRemarks property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrRemarks(String value) {
        this.editableAttrRemarks = value;
    }

    /**
     * Gets the value of the editableAttrPhenom property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrPhenom() {
        return editableAttrPhenom;
    }

    /**
     * Sets the value of the editableAttrPhenom property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrPhenom(String value) {
        this.editableAttrPhenom = value;
    }

    /**
     * Gets the value of the editableAttrPhenom2 property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrPhenom2() {
        return editableAttrPhenom2;
    }

    /**
     * Sets the value of the editableAttrPhenom2 property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrPhenom2(String value) {
        this.editableAttrPhenom2 = value;
    }

    /**
     * Gets the value of the editableAttrPhenomName property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrPhenomName() {
        return editableAttrPhenomName;
    }

    /**
     * Sets the value of the editableAttrPhenomName property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrPhenomName(String value) {
        this.editableAttrPhenomName = value;
    }

    /**
     * Gets the value of the editableAttrPhenomLat property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrPhenomLat() {
        return editableAttrPhenomLat;
    }

    /**
     * Sets the value of the editableAttrPhenomLat property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrPhenomLat(String value) {
        this.editableAttrPhenomLat = value;
    }

    /**
     * Gets the value of the editableAttrPhenomLon property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrPhenomLon() {
        return editableAttrPhenomLon;
    }

    /**
     * Sets the value of the editableAttrPhenomLon property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrPhenomLon(String value) {
        this.editableAttrPhenomLon = value;
    }

    /**
     * Gets the value of the editableAttrPhenomPressure property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrPhenomPressure() {
        return editableAttrPhenomPressure;
    }

    /**
     * Sets the value of the editableAttrPhenomPressure property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrPhenomPressure(String value) {
        this.editableAttrPhenomPressure = value;
    }

    /**
     * Gets the value of the editableAttrPhenomMaxWind property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrPhenomMaxWind() {
        return editableAttrPhenomMaxWind;
    }

    /**
     * Sets the value of the editableAttrPhenomMaxWind property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrPhenomMaxWind(String value) {
        this.editableAttrPhenomMaxWind = value;
    }

    /**
     * Gets the value of the editableAttrFreeText property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrFreeText() {
        return editableAttrFreeText;
    }

    /**
     * Sets the value of the editableAttrFreeText property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrFreeText(String value) {
        this.editableAttrFreeText = value;
    }

    /**
     * Gets the value of the editableAttrTrend property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrTrend() {
        return editableAttrTrend;
    }

    /**
     * Sets the value of the editableAttrTrend property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrTrend(String value) {
        this.editableAttrTrend = value;
    }

    /**
     * Gets the value of the editableAttrMovement property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrMovement() {
        return editableAttrMovement;
    }

    /**
     * Sets the value of the editableAttrMovement property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrMovement(String value) {
        this.editableAttrMovement = value;
    }

    /**
     * Gets the value of the editableAttrPhenomSpeed property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrPhenomSpeed() {
        return editableAttrPhenomSpeed;
    }

    /**
     * Sets the value of the editableAttrPhenomSpeed property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrPhenomSpeed(String value) {
        this.editableAttrPhenomSpeed = value;
    }

    /**
     * Gets the value of the editableAttrPhenomDirection property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrPhenomDirection() {
        return editableAttrPhenomDirection;
    }

    /**
     * Sets the value of the editableAttrPhenomDirection property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrPhenomDirection(String value) {
        this.editableAttrPhenomDirection = value;
    }

    /**
     * Gets the value of the editableAttrLevel property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrLevel() {
        return editableAttrLevel;
    }

    /**
     * Sets the value of the editableAttrLevel property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrLevel(String value) {
        this.editableAttrLevel = value;
    }

    /**
     * Gets the value of the editableAttrLevelInfo1 property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrLevelInfo1() {
        return editableAttrLevelInfo1;
    }

    /**
     * Sets the value of the editableAttrLevelInfo1 property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrLevelInfo1(String value) {
        this.editableAttrLevelInfo1 = value;
    }

    /**
     * Gets the value of the editableAttrLevelInfo2 property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrLevelInfo2() {
        return editableAttrLevelInfo2;
    }

    /**
     * Sets the value of the editableAttrLevelInfo2 property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrLevelInfo2(String value) {
        this.editableAttrLevelInfo2 = value;
    }

    /**
     * Gets the value of the editableAttrLevelText1 property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrLevelText1() {
        return editableAttrLevelText1;
    }

    /**
     * Sets the value of the editableAttrLevelText1 property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrLevelText1(String value) {
        this.editableAttrLevelText1 = value;
    }

    /**
     * Gets the value of the editableAttrLevelText2 property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrLevelText2() {
        return editableAttrLevelText2;
    }

    /**
     * Sets the value of the editableAttrLevelText2 property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrLevelText2(String value) {
        this.editableAttrLevelText2 = value;
    }

    /**
     * Gets the value of the editableAttrAltLevel property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrAltLevel() {
        return editableAttrAltLevel;
    }

    /**
     * Sets the value of the editableAttrAltLevel property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrAltLevel(String value) {
        this.editableAttrAltLevel = value;
    }

    /**
     * Gets the value of the editableAttrAltLevelInfo1 property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrAltLevelInfo1() {
        return editableAttrAltLevelInfo1;
    }

    /**
     * Sets the value of the editableAttrAltLevelInfo1 property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrAltLevelInfo1(String value) {
        this.editableAttrAltLevelInfo1 = value;
    }

    /**
     * Gets the value of the editableAttrAltLevelInfo2 property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrAltLevelInfo2() {
        return editableAttrAltLevelInfo2;
    }

    /**
     * Sets the value of the editableAttrAltLevelInfo2 property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrAltLevelInfo2(String value) {
        this.editableAttrAltLevelInfo2 = value;
    }

    /**
     * Gets the value of the editableAttrAltLevelText1 property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrAltLevelText1() {
        return editableAttrAltLevelText1;
    }

    /**
     * Sets the value of the editableAttrAltLevelText1 property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrAltLevelText1(String value) {
        this.editableAttrAltLevelText1 = value;
    }

    /**
     * Gets the value of the editableAttrAltLevelText2 property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrAltLevelText2() {
        return editableAttrAltLevelText2;
    }

    /**
     * Sets the value of the editableAttrAltLevelText2 property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrAltLevelText2(String value) {
        this.editableAttrAltLevelText2 = value;
    }

    /**
     * Gets the value of the editableAttrFromLine property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrFromLine() {
        return editableAttrFromLine;
    }

    /**
     * Sets the value of the editableAttrFromLine property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrFromLine(String value) {
        this.editableAttrFromLine = value;
    }

    /**
     * Gets the value of the editableAttrFir property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrFir() {
        return editableAttrFir;
    }

    /**
     * Sets the value of the editableAttrFir property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrFir(String value) {
        this.editableAttrFir = value;
    }

    /**
     * Gets the value of the editableAttrCarSamBackupMode property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEditableAttrCarSamBackupMode() {
        return editableAttrCarSamBackupMode;
    }

    /**
     * Sets the value of the editableAttrCarSamBackupMode property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEditableAttrCarSamBackupMode(String value) {
        this.editableAttrCarSamBackupMode = value;
    }
}
