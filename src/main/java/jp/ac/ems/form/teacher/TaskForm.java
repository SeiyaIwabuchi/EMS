package jp.ac.ems.form.teacher;

import java.util.List;

import javax.validation.constraints.Size;

import jp.ac.ems.form.teacher.ConfirmQuestionForm;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 先生用課題Form(question task for teacher).
 * @author tejc999999
 */
@Data
@NoArgsConstructor
public class TaskForm {

    /**
     * 課題ID(task id).
     */
    private String id;

    /**
     * タイトル(title).
     */
    @Size(max=50)
    private String title;

    /**
     * 説明文(description).
     */
    @Size(max=500)
    private String description;
    
    /**
     * 問題IDリスト(question id list).
     */
    private List<String> questionCheckedList;

    /**
     * 選択年度(select year).
     */
    private String selectYear;
    
    /**
     * 選択大分類(select large field).
     */
    private String selectFieldL;
    
    /**
     * 選択中分類(select middle field).
     */
    private String selectFieldM;
    
    /**
     * 選択小分類（select small field).
     */
    private String selectFieldS;
    
    /**
     * 対象コースIDリスト(target course list).
     */
    private List<String> courseCheckedList;

    /**
     * 対象クラスIDリスト(target class list).
     */
    private List<String> classCheckedList;
    
    /**
     * 対象ユーザIDリスト(target user list).
     */
    private List<String> userCheckedList;
}
