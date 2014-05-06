package gui;

/** 
 * This class is used to watch a thread termination (for instance
 * Decoder or Encoder instance) and finally notifies the ThreadSurvey
 * instance. */
public class ThreadWatch implements Runnable {
    /** Reference to the object using this ThreadWatch */
    private ThreadSurvey src = null;

    /** Reference to the thread to watch */
    private Thread thread = null;

    /** Class constructor */
    public ThreadWatch(ThreadSurvey src,Thread thread) {
	this.src = src;
	this.thread = thread;
    }
    
    /** Thread's starting method */
    public void run() {
	thread.run();
	src.terminatedThread();
    }
}
