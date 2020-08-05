package jp.ac.ems.impl.service.student;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jp.ac.ems.bean.QuestionBean;
import jp.ac.ems.bean.StudentTaskHistoryBean;
import jp.ac.ems.bean.StudentTaskQuestionHistoryBean;
import jp.ac.ems.bean.TaskBean;
import jp.ac.ems.bean.UserBean;
import jp.ac.ems.config.ExamDivisionCode;
import jp.ac.ems.config.FieldLarge;
import jp.ac.ems.config.FieldMiddle;
import jp.ac.ems.config.FieldSmall;
import jp.ac.ems.form.student.QuestionForm;
import jp.ac.ems.form.student.TaskForm;
import jp.ac.ems.repository.QuestionRepository;
import jp.ac.ems.repository.StudentTaskHistoryRepository;
import jp.ac.ems.repository.StudentTaskQuestionHistoryRepository;
import jp.ac.ems.repository.TaskRepository;
import jp.ac.ems.repository.UserRepository;
import jp.ac.ems.service.student.StudentTaskService;

/**
 * 学生用課題Serviceクラス（student task Service Class）.
 * @author tejc999999
 */
@Service
public class StudentTaskServiceImpl implements StudentTaskService {
	
    /**
     * 課題リポジトリ(task repository).
     */
    @Autowired
    TaskRepository taskRepository;

    /**
     * ユーザーリポジトリ(user repository).
     */
    @Autowired
    UserRepository userRepository;

    /**
     * 問題リポジトリ(question repository).
     */
    @Autowired
    QuestionRepository questionRepository;

    /**
     * 課題-問題履歴リポジトリ(task-question history repository).
     */
    @Autowired
    StudentTaskQuestionHistoryRepository studentTaskQuestionTaskHistoryRepository;
    
    /**
     * 課題履歴リポジトリ(task history repository).
     */
    @Autowired
    StudentTaskHistoryRepository studentTaskHistoryRepository;

    /**
     * ユーザに紐づく全ての課題を取得する.
     * @param userId ユーザID(user id)
     * @return 全ての問題Formリスト(list of all question forms)
     */
    @Override
    public List<TaskForm> findAllByLoginStudentId() {
    	
    	// 一覧画面にPOSTする時にauthentication情報からユーザ名を送る
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        List<TaskForm> taskFormList = new ArrayList<>();

        List<String> taskIdList = new ArrayList<String>();
        Optional<UserBean> optUser = userRepository.findByIdFetchUserTask(userId);
        optUser.ifPresent(userBean -> {
        	taskIdList.addAll(userBean.getTaskIdList());
        });
    	for(String taskId : taskIdList) {
            Optional<TaskBean> optTask = taskRepository.findById(Long.valueOf(taskId));
            optTask.ifPresent(taskBean -> {
            	TaskForm taskForm = new TaskForm();
            	taskForm.setId(String.valueOf(taskBean.getId()));
            	taskForm.setTitle(taskBean.getTitle());
            	taskForm.setDescription(taskBean.getDescription());
            	taskForm.setQuestionSize(String.valueOf(taskBean.getQuestionSize()));
            	taskFormList.add(taskForm);
            });
    	}
    	
    	// 回答済問題数を取得する
    	for(TaskForm taskForm : taskFormList) {
    		List<StudentTaskQuestionHistoryBean> studentTaskQuestionHistoryBeanList = studentTaskQuestionTaskHistoryRepository.findByUserIdAndTaskId(userId, Long.valueOf(taskForm.getId()));
    		if(studentTaskQuestionHistoryBeanList == null || studentTaskQuestionHistoryBeanList.size() == 0) {
    			taskForm.setAnsweredQuestionCnt("0");
    		} else {
    			int answeredQuestionCnt = 0;
    			for(StudentTaskQuestionHistoryBean studentTaskQuestionHistoryBean : studentTaskQuestionHistoryBeanList) {
    				if(studentTaskQuestionHistoryBean.getAnswer() != null && studentTaskQuestionHistoryBean.getAnswer() > 0) {
    					answeredQuestionCnt++;
    				}
    			}
    			taskForm.setAnsweredQuestionCnt(String.valueOf(answeredQuestionCnt));
    		}
    	}
        return taskFormList;
    }
    
    /**
     * 課題Formに問題Formをセットする
     * 
     * @param form 課題Form(task form)
     * @param position 位置情報(position info)
     * @return 課題Form(task form)
     */
    public TaskForm getQuestionForm(TaskForm form, int position) {

    	Map<String, String> questionMap = new HashMap<>();
    	Optional<TaskBean> optTask = taskRepository.findByIdFetchTaskQuestion(Long.valueOf(form.getId()));
    	optTask.ifPresent(taskBean -> {
    		form.setTitle(taskBean.getTitle());
    		form.setQuestionSize(String.valueOf(taskBean.getQuestionSize()));
    		questionMap.putAll(taskBean.getQuestionIdSeqMap());
    	});

    	// 指定位置情報の問題を取得する
 		String questionId = null;
 		if(position == 0) {
 			questionId = questionMap.get("0");
 			form.setQuestionCnt("1");
 		} else {
	    	String currentQuestionId = form.getQuestionForm().getId();
	    	String currentPosition = questionMap
	    									.entrySet()
	    									.stream()
	    									.filter(entry -> currentQuestionId.equals(entry.getValue()))
	    									.map(Map.Entry::getKey)
	    									.findFirst().get();
	    	String positionStr = String.valueOf(Integer.valueOf(currentPosition) + position);
	    	questionId = questionMap.get(positionStr);
	    	form.setQuestionCnt(String.valueOf(Integer.parseInt(positionStr) + 1));
    	}
    	QuestionForm questionForm = new QuestionForm();
    	Optional<QuestionBean> optQuestion = questionRepository.findById(Long.valueOf(questionId));
    	optQuestion.ifPresent(questionBean -> {
    		// データ型が違うためコピーされない
    		questionForm.setId(String.valueOf(questionBean.getId()));
    		questionForm.setCorrect(String.valueOf(questionBean.getCorrect()));
    		questionForm.setDivision(questionBean.getDivision());
    		questionForm.setYear(questionBean.getYear());
    		questionForm.setTerm(questionBean.getTerm());
    		questionForm.setNumber(String.valueOf(questionBean.getNumber()));
    		questionForm.setFieldLId(String.valueOf(questionBean.getFieldLId()));
    		questionForm.setFieldMId(String.valueOf(questionBean.getFieldMId()));
    		questionForm.setFieldSId(String.valueOf(questionBean.getFieldSId()));
    	});
		String imagePath = questionForm.getYear() + "_" + questionForm.getTerm()
			+ "\\" + String.format("%02d", Integer.parseInt(questionForm.getNumber())) + ".jpg";
		questionForm.setImagePath(imagePath);
		
		// 回答履歴がある場合、回答をコピーする
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
		Optional<StudentTaskQuestionHistoryBean> optStudentTaskQuestionHistory
			= studentTaskQuestionTaskHistoryRepository.findByUserIdAndTaskIdAndQuestionId(userId,
															Long.valueOf(form.getId()),
															Long.valueOf(questionId));
		optStudentTaskQuestionHistory.ifPresent(bean->{
			questionForm.setAnswer(String.valueOf(bean.getAnswer()));
		});

    	form.setQuestionForm(questionForm);

    	// 問題情報文字列を作成し、Formにセットする
    	StringBuffer questionInfoStrBuff = new StringBuffer();
    	String yearStr = questionForm.getYear().substring(0, 1);
    	if("H".equals(yearStr)) {
    		questionInfoStrBuff.append("平成");
    	} else if("R".equals(yearStr)) {
    		questionInfoStrBuff.append("令和");
    	}
		questionInfoStrBuff.append(questionForm.getYear().substring(1, 3) + "年");
    	String termStr = questionForm.getTerm();
    	if("H".equals(termStr)) {
    		questionInfoStrBuff.append("春");
    	} else if("A".equals(termStr)) {
    		questionInfoStrBuff.append("秋");
    	}
		questionInfoStrBuff.append("期 第" + questionForm.getNumber() + "問");
    	form.setQuestionInfoStr(questionInfoStrBuff.toString());
    	
    	// 問題分野情報文字列を作成し、Formにセットする
    	form.setQuestionFieldInfoStr(
    			FieldLarge.getName(ExamDivisionCode.AP.getName(), Byte.valueOf(questionForm.getFieldLId())) + "/"
    			+ FieldMiddle.getName(ExamDivisionCode.AP.getName(), Byte.valueOf(questionForm.getFieldMId())) + "/"
    			+ FieldSmall.getName(ExamDivisionCode.AP.getName(), Byte.valueOf(questionForm.getFieldSId())));

    	return form;
    }
    
    /**
     * 問題への回答を保存する(save answer for question).
     * @param form 課題Form(task form)
     */
    @Override
    public void answerSave(TaskForm form) {

    	if(form.getQuestionForm().getAnswer() != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = auth.getName();
    		
        	// 問題履歴を更新する
            // IDを設定
    		StudentTaskQuestionHistoryBean studentTaskQuestionHistoryBean = new StudentTaskQuestionHistoryBean();
    		Optional<StudentTaskQuestionHistoryBean> optStudentTaskQuestionHistory
    			= studentTaskQuestionTaskHistoryRepository.findByUserIdAndTaskIdAndQuestionId(userId,
    															Long.valueOf(form.getId()),
    															Long.valueOf(form.getQuestionForm().getId()));
    		optStudentTaskQuestionHistory.ifPresent(bean->{
    			studentTaskQuestionHistoryBean.setId(bean.getId());
    		});
    		// 回答情報を設定
    		studentTaskQuestionHistoryBean.setTaskId(Long.valueOf(form.getId()));
    		studentTaskQuestionHistoryBean.setQuestionId(Long.valueOf(form.getQuestionForm().getId()));
    		studentTaskQuestionHistoryBean.setAnswer(Byte.valueOf(form.getQuestionForm().getAnswer()));
    		studentTaskQuestionHistoryBean.setUpdateDate(new Date());
    		studentTaskQuestionHistoryBean.setUserId(userId);
    		
    		studentTaskQuestionTaskHistoryRepository.save(studentTaskQuestionHistoryBean);
    	}
    }
    
    /**
     * 回答アイテム取得
     * 
     * @return 回答アイテムマップ
     */
    @Override
    public Map<String,String> getAnswerSelectedItems(){
        Map<String, String> selectMap = new LinkedHashMap<String, String>();
        selectMap.put("1", "ア");
        selectMap.put("2", "イ");
        selectMap.put("3", "ウ");
        selectMap.put("4", "エ");
        return selectMap;
    }
    
    /**
     * 課題提出(submission of task).
     * 
     * @param taskForm 課題Form(task form)
     */
    @Override
    public void submissionTask(TaskForm task) {
    	// 課題履歴を作成し、提出済みとする
    	StudentTaskHistoryBean studenttaskHistoryBean = new StudentTaskHistoryBean();
    	studenttaskHistoryBean.setTaskId(taskId);
    	studenttaskHistoryBean.setUserId(userId);
    	studenttaskHistoryBean.setUpdateDate(new Date());
    	studenttaskHistoryBean.setAnswerFlg(true);
    	studentTaskHistoryRepository.save(studenttaskHistoryBean);
    	
    	// 課題を提出済みにする
    }
    
}
