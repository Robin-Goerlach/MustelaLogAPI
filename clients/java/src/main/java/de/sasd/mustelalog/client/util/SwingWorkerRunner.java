package de.sasd.mustelalog.client.util;

import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Small helper to reduce repetitive SwingWorker boilerplate.
 */
public final class SwingWorkerRunner
{
    private SwingWorkerRunner() {}

    public static <T> void run(Callable<T> task, Consumer<T> success, Consumer<Throwable> error)
    {
        new SwingWorker<T, Void>()
        {
            @Override
            protected T doInBackground() throws Exception
            {
                return task.call();
            }

            @Override
            protected void done()
            {
                try
                {
                    success.accept(get());
                }
                catch (Exception exception)
                {
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    error.accept(cause);
                }
            }
        }.execute();
    }
}
