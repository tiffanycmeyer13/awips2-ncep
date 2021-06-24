package gov.noaa.nws.ncep.ui.nctextui.dbutil;

/**
 *
 * gov.noaa.nws.ncep.ui.nctextui.dbutil.EReportTimeRange
 * 
 * <pre>
 * SOFTWARE HISTORY
 *
 * Date         Ticket#     Engineer    Description
 * -------      -------     --------    -----------
 * 04/14/2020   76579       k sunil     Added LATEST in the enum
 *
 * </pre>
 *
 * @author Chin Chen
 */

public enum EReportTimeRange {

    NONE(0),
    ONE_HOUR(1),
    THREE_HOURS(3),
    SIX_HOURS(6),
    TWELVE_HOURS(12),
    TWENTYFOUR_HOURS(24),
    FORTYEIGHT_HOURS(48),
    LATEST(-1);
    private int timeRangeVal;

    // Constructor
    EReportTimeRange(int p) {
        timeRangeVal = p;
    }

    // Overloaded constructor
    EReportTimeRange() {
        timeRangeVal = -1;
    }

    public int getTimeRange() {
        return timeRangeVal;
    }

    public EReportTimeRange getTimeRangeFromInt(int i) {
        EReportTimeRange[] vals = EReportTimeRange.values();
        for (EReportTimeRange timeRange : vals) {
            if (timeRange.getTimeRange() == i) {
                return timeRange;
            }
        }
        return NONE;
    }
}