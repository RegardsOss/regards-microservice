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
package fr.cnes.regards.framework.modules.jobs.service.stub;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.modules.jobs.domain.JobInfo;
import fr.cnes.regards.framework.modules.jobs.domain.JobStatus;
import fr.cnes.regards.framework.modules.jobs.service.systemservice.IJobInfoSystemService;

/**
 *
 */
@Service
public class JobInfoSystemServiceStub implements IJobInfoSystemService {

    @Override
    public JobInfo findJobInfo(final String pTenantName, final Long pJobInfoId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JobInfo updateJobInfo(final String pTenantId, final JobInfo pJobInfo) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JobInfo updateJobInfoToDone(final Long pJobInfoId, final JobStatus pJobStatus, final String pTenantName) {
        // TODO Auto-generated method stub
        return null;
    }

}
