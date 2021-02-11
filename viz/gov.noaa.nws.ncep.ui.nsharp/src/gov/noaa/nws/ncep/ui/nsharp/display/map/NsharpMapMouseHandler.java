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

import gov.noaa.nws.ncep.ui.nsharp.view.AbstractNsharpLoadDialog;
import gov.noaa.nws.ncep.ui.nsharp.view.NsharpLoadDialog;

/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.display.map.NsharpMapMouseHandler
 * 
 * This java class performs the NSHARP Modal functions.
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 03/23/2010   229         Chin Chen   Initial coding
 * 03/11/2013   972         Greg Hull   NatlCntrsEditor
 * 09/28/2015   RM#10295    Chin Chen   Let sounding data query run in its own
 *                                      thread to avoid gui locked out during load
 * 03/31/2020   73571       smanoj      NSHARP D2D port refactor
 *
 * </pre>
 * 
 * @author Chin Chen
 */
public class NsharpMapMouseHandler extends AbstractNsharpMapMouseHandler {

    @Override
    public AbstractNsharpLoadDialog getLoadDialog() {
        return NsharpLoadDialog.getAccess();
    }
}
