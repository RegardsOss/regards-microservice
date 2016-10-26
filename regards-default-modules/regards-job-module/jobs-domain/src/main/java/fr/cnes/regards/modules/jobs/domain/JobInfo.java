/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.jobs.domain;

import java.nio.file.Path;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import org.springframework.hateoas.Identifiable;

import fr.cnes.regards.modules.jobs.domain.converters.JobOuputConverter;
import fr.cnes.regards.modules.jobs.domain.converters.JobParameterConverter;
import fr.cnes.regards.modules.jobs.domain.converters.PathConverter;

/**
 * Store Job Information
 */
@Entity(name = "T_JOB_INFO")
public class JobInfo implements Identifiable<Long> {

    /**
     * JobInfo id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "job_sequence", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "job_sequence", allocationSize = 10)
    private Long id;

    /**
     * Job priority
     */
    @Column(name = "priority")
    private int priority;

    /**
     * Job workspace
     */
    @Column(name = "workspace", columnDefinition = "LONGVARCHAR")
    @Convert(converter = PathConverter.class)
    private Path workspace;

    /**
     * Job result (nullable)
     */
    @Column(name = "result", columnDefinition = "LONGVARCHAR")
    @Convert(converter = JobOuputConverter.class)
    private List<Output> result;

    /**
     * Job parameters
     */
    @Column(name = "parameters", columnDefinition = "LONGVARCHAR")
    @Convert(converter = JobParameterConverter.class)
    private JobParameters parameters;

    /**
     * Job owner
     */
    @Column(name = "owner")
    private String owner;

    /**
     * Job class to execute
     */
    @Column(name = "className")
    private String className;

    /**
     * Hield caracteristics of this job. Saved on cascade
     */
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "status_id", foreignKey = @ForeignKey(name = "FK_JOB_STATUS_INFO"))
    private StatusInfo status;

    /**
     * When true, the job is done, results deleted, job metadata archived
     */
    @Column(name = "archived")
    private boolean archived;

    public JobInfo() {
        super();
    }

    public JobInfo(final JobConfiguration pJobConfiguration) {
        this();
        className = pJobConfiguration.getClassName();
        parameters = pJobConfiguration.getParameters();
        owner = pJobConfiguration.getOwner();
        status = pJobConfiguration.getStatusInfo();
        workspace = pJobConfiguration.getWorkspace();
        priority = pJobConfiguration.getPriority();
    }

    /**
     * @param pId
     *            the id to set
     */
    public void setId(final Long pId) {
        id = pId;
    }

    /**
     *
     * @return job id
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param pPriority
     *            the priority to set
     */
    public void setPriority(final int pPriority) {
        priority = pPriority;
    }

    /**
     * @param pParameters
     *            the parameters to set
     */
    public void setParameters(final JobParameters pParameters) {
        parameters = pParameters;
    }

    /**
     * @param pOwner
     *            the owner to set
     */
    public void setOwner(final String pOwner) {
        owner = pOwner;
    }

    /**
     * @return the result
     */
    public List<Output> getResult() {
        return result;
    }

    /**
     * @param pResult
     *            the result to set
     */
    public void setResult(final List<Output> pResult) {
        result = pResult;
    }

    /**
     * @return the parameters
     */
    public JobParameters getParameters() {
        return parameters;
    }

    /**
     * A specific parameter
     *
     * @return the workspace, where files are located
     */
    public Path getWorkspace() {
        return workspace;
    }

    /**
     *
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param pClassName
     *            the className to set
     */
    public void setClassName(final String pClassName) {
        className = pClassName;
    }

    /**
     * @param pStatus
     *            the status to set
     */
    public void setStatus(final StatusInfo pStatus) {
        status = pStatus;
    }

    public final int getPriority() {
        return this.priority;
    }

    public void setWorkspace(final Path pWorkspace) {
        workspace = pWorkspace;
    }

    public StatusInfo getStatus() {
        return status;
    }

    public boolean needWorkspace() {
        return workspace != null;
    }

    /**
     * @return job is archived
     */
    public boolean isArchived() {
        return archived;
    }

    /**
     * @param pArchived
     *            set if job archived
     */
    public void setArchived(final boolean pArchived) {
        archived = pArchived;
    }

}