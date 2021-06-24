/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 *
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 *
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 *
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package gov.noaa.nws.ncep.ui.nsharp.display.map;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.display.map.NsharpMapResource
 * 
 * This java class performs the NSHARP Resource functions.
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 03/23/2010   229         Chin Chen   Initial coding
 * 08/17/2012   655         B. Hebbard  Added paintProps as parameter to IDisplayable draw (2)
 * 03/11/2013   972         Greg Hull   NatlCntrsEditor
 * 03/31/2020   73571       smanoj      NSHARP D2D port refactor
 * 
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpMapResource extends AbstractNsharpMapResource {

    public NsharpMapResource(AbstractNsharpMapResourceData resourceData,
            LoadProperties loadProperties) {
        super(resourceData, loadProperties);
    }

    @Override
    public IInputHandler getMouseHandler() {
        if (mouseHandler == null) {
            mouseHandler = new NsharpMapMouseHandler();
        }
        return mouseHandler;
    }

    @Override
    public AbstractNsharpMapResourceData getNewMapResourceData() {
        return new NsharpMapResourceData();
    }
}
