package fr.cnes.regards.framework.modules.jobs.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.util.FileSystemUtils;

import com.google.common.collect.BiMap;
import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.modules.jobs.domain.JobInfo;
import fr.cnes.regards.framework.modules.jobs.domain.JobStatus;
import fr.cnes.regards.framework.modules.jobs.domain.event.*;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;

/**
 * Job specific ThreadPoolExecutor.
 * Update JobInfo status between and after execution of associated job
 * @author oroussel
 */
public class JobThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * Only for the thread names
     */
    private static final ThreadFactory THREAD_FACTORY = new DefaultJobThreadFactory();

    private IJobInfoService jobInfoService;

    private IRuntimeTenantResolver runtimeTenantResolver;

    private BiMap<JobInfo, RunnableFuture<Void>> jobsMap;

    private IPublisher publisher;

    public JobThreadPoolExecutor(int poolSize, IJobInfoService jobInfoService,
            BiMap<JobInfo, RunnableFuture<Void>> jobsMap, IRuntimeTenantResolver runtimeTenantResolver,
            IPublisher publisher) {
        super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), THREAD_FACTORY);
        this.jobInfoService = jobInfoService;
        this.jobsMap = jobsMap;
        this.runtimeTenantResolver = runtimeTenantResolver;
        this.publisher = publisher;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        JobInfo jobInfo = jobsMap.inverse().get(r);
        // In case jobsMap is not yet available (this means afterExecute has been called very very early)
        // because of jobsMap.put(jobInfo, threadPool.submit(...))
        while (jobInfo == null) {
            jobInfo = jobsMap.inverse().get(r);
        }
        runtimeTenantResolver.forceTenant(jobInfo.getTenant());
        jobInfo.updateStatus(JobStatus.RUNNING);
        jobInfoService.save(jobInfo);
        publisher.publish(new JobEvent(jobInfo.getId(), JobEventType.RUNNING));
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        JobInfo jobInfo = jobsMap.inverse().get(r);
        runtimeTenantResolver.forceTenant(jobInfo.getTenant());
        // FutureTask (which is used by ThreadPoolExecutor) doesn't give a fuck of thrown exception so we must get it
        // by hands
        if ((t == null) && (r instanceof Future<?>)) {
            try {
                ((Future<?>) r).get();
            } catch (CancellationException ce) {
                t = ce;
                jobInfo.updateStatus(JobStatus.ABORTED);
                jobInfoService.save(jobInfo);
                publisher.publish(new JobEvent(jobInfo.getId(), JobEventType.ABORTED));
            } catch (ExecutionException ee) { // NOSONAR
                t = ee.getCause();
                jobInfo.updateStatus(JobStatus.FAILED);
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                jobInfo.getStatus().setStackTrace(sw.toString());
                jobInfoService.save(jobInfo);
                publisher.publish(new JobEvent(jobInfo.getId(), JobEventType.FAILED));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/reset
            }
        }
        // If no error
        if (t == null) {
            jobInfo.updateStatus(JobStatus.SUCCEEDED);
            jobInfo.setResult(jobInfo.getJob().getResult());
            jobInfoService.save(jobInfo);
            publisher.publish(new JobEvent(jobInfo.getId(), JobEventType.SUCCEEDED));
        }
        // Delete complete workspace dir if job has one
        if (jobInfo.getJob().needWorkspace()) {
            FileSystemUtils.deleteRecursively(jobInfo.getJob().getWorkspace().toFile());
        }
        // Clean jobsMap
        jobsMap.remove(jobInfo);
    }

    /**
     * The default thread factory
     */
    private static final class DefaultJobThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadGroup group;

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final String namePrefix;

        private DefaultJobThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "job-pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

}