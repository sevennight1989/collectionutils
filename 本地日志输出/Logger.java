
import android.util.Log;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class Logger implements Runnable {
    static String TAG = "PengLog";
    private static final int VERBOSE = 1;
    private static final int INFO = 2;
    private static final int DEBUG = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;

    //总日志输出等级:大于此等级的日志才会输出
    private static int LOGLEVEL = 0;
    //日志写文件等级:大于此等级的日志才会写入文件
    private static int WRITELOGLEVEL = 2;

    //日志写文件开关 true为开,false为关
    private static boolean LOGM_WRITE_TO_FILE = true;
    //日志打印logcat开关  true为开,false为关
    private static boolean LOGM_LOGCAT_SWITCH = BuildConfig.DEBUG;

    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyyMMdd");// 日志文件格式
    private static SimpleDateFormat logMSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");// 日志的输出格式
    private static String LOGM_PATH_SDCARD_DIR = "/sdcard" + File.separator + "llog" + File.separator + "LLog/";// 日志文件在sdcard中的路径
    private static int SDCARD_LOG_FILE_SAVE_DAYS = 0;// sd卡中日志文件的最多保存天数
    private static String LOGMFILEName = "Log.txt";// 本类输出的日志文件名称

    private static LoggerPA instance = null;
    private boolean isInited = false;
    private boolean isExited = false;
    private BufferedWriter output = null;
    private Queue<String> queue = null;

    private Thread thread = null;

    private LoggerPA() {
        queue = new LinkedList<String>();
    }

    public synchronized static LoggerPA getInstance() {
        if (instance == null) {
            instance = new LoggerPA();
        }
        return instance;
    }

    public synchronized static void releaseInstance() {
        if (instance != null) {
            instance = null;
        }
    }

    public synchronized boolean init() {
        if (isInited) {
            return true;
        }
        try {
            //String needWriteFile = DateUtils.getNowShortShortStr();
            String needWriteFile = logfile.format(new Date(System.currentTimeMillis()));
            File dir = new File((LOGM_PATH_SDCARD_DIR + needWriteFile));
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File f = new File(LOGM_PATH_SDCARD_DIR + needWriteFile, LOGMFILEName);
            if (!f.exists()) {
                new File(f.getAbsolutePath()).createNewFile();
                //FileUtils.createFile(f.getAbsolutePath());
            }
            output = new BufferedWriter(new FileWriter(f, true));
            isExited = false;
            startThread();
            isInited = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isInited;
    }

    public synchronized void dispose() {
        if (!isInited) {
            return;
        }
        isExited = true;
        isInited = false;
        synchronized (thread) {
            if (thread.isAlive()) {
                try {
                    thread.notifyAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            output = null;
        }
        thread = null;
    }

    public void add(String log) {
        if (!isInited) {
            return;
        }
        synchronized (queue) {
            queue.add(log);
        }
        synchronized (thread) {
            thread.notifyAll();
        }
    }

    private String get() {
        String result = null;
        synchronized (queue) {
            if (!queue.isEmpty()) {
                result = queue.poll();
            }
        }

        return result;
    }

    private boolean isEmpty() {
        boolean result = true;
        synchronized (queue) {
            result = queue.isEmpty();
        }

        return result;
    }

    @Override
    public void run() {
        while (!isExited) {
            if (isEmpty()) {
                synchronized (thread) {
                    try {
                        thread.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            while (!isEmpty()) {
                String log = get();
                try {
                    output.write(log);
                    output.newLine();
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startThread() {
        thread = new Thread(this);
        thread.start();
    }

    public static void setLOGM_LOGCAT_SWITCH(boolean lOGM_LOGCAT_SWITCH) {
        LOGM_LOGCAT_SWITCH = lOGM_LOGCAT_SWITCH;
    }


    private static void addLog(int level, final String mylogtype, final String tag, final String text) {
        if (!LOGM_WRITE_TO_FILE || (WRITELOGLEVEL >= level)) {
            return;
        }
        StringBuilder needWriteMessage = new StringBuilder().append(logMSdf.format(new Date())).append(" ").append(mylogtype).append(" ").append(tag).append(" ").append(text);
        getInstance().add(needWriteMessage.toString());
        needWriteMessage = null;
    }


    public static void v(String msg) {
        if (VERBOSE > LOGLEVEL) {
            if (LOGM_LOGCAT_SWITCH) {
                Log.v(TAG, msg);
            }
            addLog(VERBOSE, "v", TAG, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (VERBOSE > LOGLEVEL) {
            if (LOGM_LOGCAT_SWITCH) {
                Log.v(tag, msg);
            }
            addLog(VERBOSE, "v", tag, msg);
        }
    }

    public static void d(String msg) {
        if (DEBUG > LOGLEVEL) {
            if (LOGM_LOGCAT_SWITCH) {
                Log.d(TAG, msg);
            }
            addLog(DEBUG, "d", TAG, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG > LOGLEVEL) {
            if (LOGM_LOGCAT_SWITCH) {
                Log.d(tag, msg);
            }
            addLog(DEBUG, "d", tag, msg);
        }
    }

    public static void i(String msg) {
        if (INFO > LOGLEVEL) {
            if (LOGM_LOGCAT_SWITCH) {
                Log.i(TAG, msg);
            }
            addLog(INFO, "i", TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (INFO > LOGLEVEL) {
            if (LOGM_LOGCAT_SWITCH) {
                Log.i(tag, msg);
            }
            addLog(INFO, "i", tag, msg);
        }
    }

    public static void w(String msg) {
        if (WARN > LOGLEVEL) {
            if (LOGM_LOGCAT_SWITCH) {
                Log.w(TAG, msg);
            }
            addLog(WARN, "w", TAG, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (WARN > LOGLEVEL) {
            if (LOGM_LOGCAT_SWITCH) {
                Log.w(tag, msg);
            }
            addLog(WARN, "w", tag, msg);
        }
    }

    public static void e(String msg) {
        if (ERROR > LOGLEVEL) {
            if (LOGM_LOGCAT_SWITCH) {
                Log.e(TAG, msg);
            }
            addLog(ERROR, "e", TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (ERROR > LOGLEVEL) {
            if (LOGM_LOGCAT_SWITCH) {
                Log.e(tag, msg);
            }
            addLog(ERROR, "e", tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable r) {
        if (ERROR > LOGLEVEL) {
            if (LOGM_LOGCAT_SWITCH) {
                Log.e(tag, msg, r);
            }
            addLog(ERROR, "e", tag, msg);
        }
    }


}
 
 
 
 