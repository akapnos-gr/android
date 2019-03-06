package gr.akapnos.app.utilities;
public abstract class RunnableArg implements Runnable {
    private Object[] m_args;
    private String mResult;
    protected RunnableArg() {}

    public void run(String result) {
        mResult = result;
        run();
    }
    public void run(Object... args) {
        setArgs(args);
        run();
    }

    public void setArgs(Object... args) {
        m_args = args;
    }

    public int getArgCount() {
        return m_args == null ? 0 : m_args.length;
    }

    public Object[] getArgs() {
        return m_args;
    }

    public String getResult() {
        return mResult == null ? "" : mResult;
    }
}