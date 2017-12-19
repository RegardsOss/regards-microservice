/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.framework.modules.jobs.service;

import java.util.List;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.modules.jobs.dao.IJobInfoRepository;
import fr.cnes.regards.framework.modules.jobs.domain.JobInfo;
import fr.cnes.regards.framework.modules.jobs.domain.JobStatus;
import fr.cnes.regards.framework.modules.jobs.domain.JobStatusInfo;
import fr.cnes.regards.framework.modules.jobs.domain.event.StopJobEvent;

/**
 * @author oroussel
 */
@Service
@MultitenantTransactional
public class JobInfoService implements IJobInfoService {

    @Autowired
    private IPublisher publisher;

    /**
     * {@link JobInfo} JPA Repository
     */
    @Autowired
    private IJobInfoRepository jobInfoRepository;

    @Override
    public JobInfo findHighestPriorityQueuedJobAndSetAsToBeRun() {
        JobInfo found = jobInfoRepository.findHighestPriorityQueued();
        if (found != null) {
            Hibernate.initialize(found.getParameters());
            found.updateStatus(JobStatus.TO_BE_RUN);
            jobInfoRepository.save(found);
        }
        return found;
    }

    @Override
    public List<JobInfo> retrieveJobs() {
        return ImmutableList.copyOf(jobInfoRepository.findAll());
    }

    @Override
    public List<JobInfo> retrieveJobs(JobStatus state) {
        return jobInfoRepository.findAllByStatusStatus(state);
    }

    @Override
    public JobInfo retrieveJob(UUID id) {
        return jobInfoRepository.findById(id);
    }

    @Override
    public JobInfo createAsPending(JobInfo jobInfo) {
        jobInfo.updateStatus(JobStatus.PENDING);
        return jobInfoRepository.save(jobInfo);
    }

    @Override
    public JobInfo createAsQueued(JobInfo jobInfo) {
        jobInfo.updateStatus(JobStatus.QUEUED);
        return jobInfoRepository.save(jobInfo);
    }

    @Override
    public JobInfo save(final JobInfo jobInfo) {
        if (jobInfo.getId() == null) {
            throw new IllegalArgumentException("Please use create method for creating, you dumb...");
        }
        return jobInfoRepository.save(jobInfo);
    }

    @Override
    public void stopJob(UUID id) {
        publisher.publish(new StopJobEvent(id));
    }

    @Override
    public void updateJobInfosCompletion(Iterable<JobInfo> jobInfos) {
        for (JobInfo jobInfo : jobInfos) {
            JobStatusInfo status = jobInfo.getStatus();
            jobInfoRepository
                    .updateCompletion(status.getPercentCompleted(), status.getEstimatedCompletion(), jobInfo.getId());
        }
    }
}