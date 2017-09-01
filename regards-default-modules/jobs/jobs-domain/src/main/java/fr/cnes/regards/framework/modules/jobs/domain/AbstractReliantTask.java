package fr.cnes.regards.framework.modules.jobs.domain;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;

import fr.cnes.regards.framework.jpa.IIdentifiable;

/**
 * Reliant task. Associated jobs of this task must not be executed while reliants task jobs are not terminated
 * This class is abstract. Please inherit it from your microservice adding your personal informations in order to use it
 * @author oroussel
 */
@Entity
@Table(name = "t_task")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractReliantTask<K extends AbstractReliantTask> implements IIdentifiable<Long> {
    @Id
    @SequenceGenerator(name = "TaskSequence", initialValue = 1, sequenceName = "seq_task")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TaskSequence")
    protected Long id;

    @OneToMany
    // Using a join table better than a foreign key on t_job_info to avoid adding a dependence. A job/job_info knows
    // nothing (like Job Snow), it just have something to do regardless of everything else
    @JoinTable(name = "ta_task_job_infos",
            joinColumns = @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "fk_task")),
            inverseJoinColumns = @JoinColumn(name = "job_info_id", foreignKey = @ForeignKey(name = "fk_job_info")),
            uniqueConstraints = @UniqueConstraint(name = "uk_job_info_id", columnNames = "job_info_id"))
    protected Set<JobInfo> jobInfos = new HashSet<>();

    @ManyToMany(targetEntity = AbstractReliantTask.class)
    @JoinTable(name = "ta_tasks_reliant_tasks",
               joinColumns = @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "fk_task_2")),
               inverseJoinColumns = @JoinColumn(name = "reliant_task_id", foreignKey = @ForeignKey(name = "fk_reliant_task")))
    protected Set<K> reliantTasks = new HashSet<>();

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public Set<JobInfo> getJobInfos() {
        return jobInfos;
    }

    public void setJobInfos(Set<JobInfo> jobInfos) {
        this.jobInfos = jobInfos;
    }

    public Set<K> getReliantTasks() {
        return reliantTasks;
    }

    public void setReliantTasks(Set<K> reliantTasks) {
        this.reliantTasks = reliantTasks;
    }
}
