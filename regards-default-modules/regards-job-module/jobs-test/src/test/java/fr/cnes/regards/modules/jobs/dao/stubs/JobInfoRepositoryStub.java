/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.jobs.dao.stubs;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.framework.test.repository.RepositoryStub;
import fr.cnes.regards.modules.jobs.dao.IJobInfoRepository;
import fr.cnes.regards.modules.jobs.domain.JobConfiguration;
import fr.cnes.regards.modules.jobs.domain.JobInfo;
import fr.cnes.regards.modules.jobs.domain.JobParameters;
import fr.cnes.regards.modules.jobs.domain.JobParametersFactory;
import fr.cnes.regards.modules.jobs.domain.JobStatus;
import fr.cnes.regards.modules.jobs.domain.Output;

/***
 * {@link PluginConfiguration} Repository stub.
 *
 * @author Christophe Mertz
 *
 */
@Repository
@Primary
@Profile("test")
public class JobInfoRepositoryStub extends RepositoryStub<JobInfo> implements IJobInfoRepository {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JobInfoRepositoryStub.class);

    public JobInfoRepositoryStub() {
        try {
            jobInfo1.getStatus().setDescription("status jobInfo1");

            List<Output> outputs = new ArrayList<>();
            outputs.add(new Output(MediaType.APPLICATION_JSON_UTF8_VALUE, new URI("http://localhost/80/file.txt")));
            outputs.add(new Output(MediaType.APPLICATION_OCTET_STREAM_VALUE, new URI("http://localhost/results.txt")));

            jobInfo1.setResult(outputs);

            setIdAndStatusAndAddToRepository(jobInfo1, 33L, JobStatus.RUNNING);
            jobInfo2.getStatus().setDescription("status jobInfo2 azertyazerty");
            setIdAndStatusAndAddToRepository(jobInfo2, 44L, JobStatus.QUEUED);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    private void setIdAndStatusAndAddToRepository(JobInfo pJobInfo, Long pId, JobStatus pStatus) {
        pJobInfo.setId(pId);
        pJobInfo.getStatus().setJobStatus(pStatus);
        getEntities().add(pJobInfo);
    }

    @Override
    public List<JobInfo> findAllByStatusStatus(JobStatus pStatus) {
        try (Stream<JobInfo> stream = getEntities().stream()) {
            final List<JobInfo> jobs = new ArrayList<>();
            stream.filter(j -> j.getStatus().getJobStatus().equals(pStatus)).forEach(j -> jobs.add(j));
            return jobs;
        }
    }

    private static final JobParameters jobParameters1 = JobParametersFactory.build().addParameter("param11", "value11")
            .addParameter("param12", "value12").addParameter("param13", "value13").getParameters();

    private final static Path workspace = FileSystems.getDefault().getPath("/home", "cmertz", "git-regards",
                                                                           "rs-microservice");

    private static JobConfiguration jobConfiguration1 = new JobConfiguration("", jobParameters1,
            "fr.cnes.regards.modules.MyCustomJob", LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(15),
            33, workspace, "owner@unix.org");

    private static final JobInfo jobInfo1 = new JobInfo(jobConfiguration1);

    private static final JobParameters jobParameters2 = JobParametersFactory.build()
            .addParameter("parameter22", "parameter value22").addParameter("parameter22", "parameter value22")
            .addParameter("parameter32", "parameter value32").addParameter("parameter42", "parameter value42")
            .getParameters();

    private static JobConfiguration jobConfiguration2 = new JobConfiguration("", jobParameters2,
            "fr.cnes.regards.modules.MyOtherCustomJob", LocalDateTime.now().plusDays(2),
            LocalDateTime.now().plusDays(15), 10, null, "master@ubuntu.org");

    private static final JobInfo jobInfo2 = new JobInfo(jobConfiguration2);

}