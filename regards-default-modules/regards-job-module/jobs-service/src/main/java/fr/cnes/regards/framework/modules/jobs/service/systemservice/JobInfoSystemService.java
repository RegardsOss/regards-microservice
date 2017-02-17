/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.modules.jobs.service.systemservice;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.modules.jobs.dao.IJobInfoRepository;
import fr.cnes.regards.framework.modules.jobs.domain.JobInfo;
import fr.cnes.regards.framework.modules.jobs.domain.JobStatus;
import fr.cnes.regards.framework.modules.jobs.service.manager.JobMonitor;

/**
 * @author Léo Mieulet
 */
@Service
public class JobInfoSystemService implements IJobInfoSystemService {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobMonitor.class);

    /**
     * {@link JobInfo} JPA Repository
     */
    private final IJobInfoRepository jobInfoRepository;

    /**
     * @param pJobInfoRepository
     *            JobInfo repository
     */
    public JobInfoSystemService(final IJobInfoRepository pJobInfoRepository) {
        super();
        jobInfoRepository = pJobInfoRepository;
    }

    @Override
    public JobInfo updateJobInfoToDone(final Long pJobInfoId, final JobStatus pJobStatus, final String pTenantName) {
        final JobInfo jobInfo = findJobInfo(pTenantName, pJobInfoId);
        if (jobInfo != null) {
            jobInfo.getStatus().setJobStatus(pJobStatus);
            jobInfo.getStatus().setStopDate(LocalDateTime.now());
            updateJobInfo(pTenantName, jobInfo);
        } else {
            LOG.error(String.format("Job not found %d", pJobInfoId));
        }
        return jobInfo;
    }

    @Override
    public JobInfo findJobInfo(final String pTenantId, final Long pJobInfoId) {
        return jobInfoRepository.findOne(pJobInfoId);
    }

    @Override
    public JobInfo updateJobInfo(final String pTenantId, final JobInfo pJobInfo) {
        return jobInfoRepository.save(pJobInfo);
    }

}