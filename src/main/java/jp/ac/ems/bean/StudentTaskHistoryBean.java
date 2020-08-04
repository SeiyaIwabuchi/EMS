package jp.ac.ems.bean;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * 学生、課題履歴Bean(student/task hisotory bean).
 * @author tejc999999
 *
 */
@Setter
@Getter
@Entity
@Table(name = "t_student_task_history")
public class StudentTaskHistoryBean {

    /**
     * 履歴コード(history code).
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 学生ID(student id).
     */
    @Column(name = "user_id")
    private String userId;

    /**
     * 課題ID(task id).
     */
    @Column(name = "task_id")
    private Long taskId;

    /**
     * 回答フラグ
     * 0:未回答, 1:回答済
     */
    @Column(name = "answer_flg")
    private Boolean answerFlg;
    
    /**
     * 更新日時(update date time).
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_date")
    private Date updateDate;
    
    /**
     * 学生・課題-問題履歴Bean：相互参照オブジェクト(user・task question history：cross reference object).
     */
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "task_history_id")
    private Set<TaskQuestionHistoryBean> TaskQuestionHistoryBeans;
}
