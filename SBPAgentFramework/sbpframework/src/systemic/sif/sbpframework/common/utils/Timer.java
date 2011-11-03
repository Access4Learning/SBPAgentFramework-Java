package systemic.sif.sbpframework.common.utils;

import java.util.Date;

public class Timer
{
  private long startTime = -1;
  private long endTime = -1;
  
  /** 
   * Starts the timer
   */
  public void start()
  {
    Date startDate = new Date();
    startTime = startDate.getTime();
  }
  
  /** 
   * Stops the timer
   */
  public void finish()
  {
    Date endDate = new Date();
    endTime = endDate.getTime(); 
  }
  
  /**
   * Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT when start() was called last.
   * @return see description
   */
  public long getStartTime()
  {
    return startTime;
  }
  
  /**
   * Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT when finish() was called last.
   * @return see description
   */
  public long getFinishTime()
  {
    return endTime;
  }
  
  /**
   * Returns the number of milliseconds passed between the last call of start() and finish().
   * @return See description.
   */
  public long timeTaken()
  {
    return endTime-startTime;
  }
}
