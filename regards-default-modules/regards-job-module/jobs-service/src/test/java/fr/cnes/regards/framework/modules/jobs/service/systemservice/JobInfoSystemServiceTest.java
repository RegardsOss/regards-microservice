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
package fr.cnes.regards.framework.modules.jobs.service.systemservice;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.cnes.regards.framework.modules.jobs.dao.IJobInfoRepository;
import fr.cnes.regards.framework.modules.jobs.domain.JobInfo;
import fr.cnes.regards.framework.modules.jobs.domain.JobStatus;
import fr.cnes.regards.framework.modules.jobs.domain.StatusInfo;
import fr.cnes.regards.framework.modules.jobs.service.systemservice.JobInfoSystemService;

/**
 *
 */
public class JobInfoSystemServiceTest {

    private IJobInfoRepository jobInfoRepository;

    private JobInfoSystemService jobInfoSystemService;

    @Before
    public void setUp() {
        jobInfoRepository = Mockito.mock(IJobInfoRepository.class);
        jobInfoSystemService = new JobInfoSystemService(jobInfoRepository);
    }

    @Test
    public void testCreate() {
        final JobInfo jobInfo = new JobInfo();
        final String pTenantId = "project1";
        jobInfoSystemService.findJobInfo(pTenantId, 1L);
    }

    @Test
    public void testUpdate() {
        final JobInfo jobInfo = new JobInfo();
        final String pTenantId = "project1";
        jobInfoSystemService.updateJobInfo(pTenantId, jobInfo);
    }

    @Test
    public void testUpdateToDone() {
        final String pTenantId = "project1";
        final JobInfo jobInfo = new JobInfo();
        jobInfo.setStatus(new StatusInfo());
        final long jobInfoId = 1L;
        Mockito.when(jobInfoRepository.findOne(jobInfoId)).thenReturn(jobInfo);

        jobInfoSystemService.updateJobInfoToDone(1L, JobStatus.QUEUED, pTenantId);
        Mockito.verify(jobInfoRepository).save(jobInfo);
    }

    @Test
    public void testUpdateUnexistantEntityToDone() {
        final JobInfo jobInfo = new JobInfo();
        final String pTenantId = "project1";
        final long jobInfoId = 1L;
        Mockito.when(jobInfoRepository.findOne(jobInfoId)).thenReturn(null);
        final JobInfo updatedJobInfo = jobInfoSystemService.updateJobInfoToDone(jobInfoId, JobStatus.QUEUED, pTenantId);
        Assertions.assertThat(updatedJobInfo).isNull();
    }
}