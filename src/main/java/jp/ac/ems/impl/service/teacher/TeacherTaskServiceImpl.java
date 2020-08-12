package jp.ac.ems.impl.service.teacher;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jp.ac.ems.bean.ClassBean;
import jp.ac.ems.bean.ClassCourseBean;
import jp.ac.ems.bean.CourseBean;
import jp.ac.ems.bean.QuestionBean;
import jp.ac.ems.bean.TaskBean;
import jp.ac.ems.bean.TaskQuestionBean;
import jp.ac.ems.bean.UserBean;
import jp.ac.ems.bean.StudentCourseBean;
import jp.ac.ems.bean.StudentTaskBean;
import jp.ac.ems.bean.StudentTaskQuestionHistoryBean;
import jp.ac.ems.config.FieldLarge;
import jp.ac.ems.config.FieldMiddle;
import jp.ac.ems.config.FieldSmall;
import jp.ac.ems.config.ExamDivisionCodeProperties;
import jp.ac.ems.config.ServerProperties;
import jp.ac.ems.form.teacher.TaskForm;
import jp.ac.ems.form.teacher.TaskSubmissionForm;
import jp.ac.ems.repository.ClassRepository;
import jp.ac.ems.repository.CourseRepository;
import jp.ac.ems.repository.QuestionRepository;
import jp.ac.ems.repository.StudentTaskQuestionHistoryRepository;
import jp.ac.ems.repository.TaskRepository;
import jp.ac.ems.repository.UserRepository;
import jp.ac.ems.service.teacher.TeacherTaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * 先生用課題Serviceクラス（teacher task Service Class）.
 * @author tejc999999
 */
@Service
public class TeacherTaskServiceImpl implements TeacherTaskService {

    /**
     * 課題リポジトリ(task repository).
     */
    @Autowired
    TaskRepository taskRepository;
    
    /**
     * 問題リポジトリ(question repository).
     */
    @Autowired
    QuestionRepository questionRepository;

    /**
     * コースリポジトリ(course repository).
     */
    @Autowired
    CourseRepository courseRepository;
    
    /**
     * クラスリポジトリ(class repository).
     */
    @Autowired
    ClassRepository classRepository;

    /**
     * ユーザーリポジトリ(user repository).
     */
    @Autowired
    UserRepository userRepository;
    
    /**
     * 学生：課題-問題履歴リポジトリ
     */
    @Autowired
    StudentTaskQuestionHistoryRepository studentTaskQuestionHistoryRepository;

    /**
     * サーバー設定プロパティ.
     */
    @Autowired
    ServerProperties serverProperties;

    /**
     * 全ての問題を取得する.
     * @return 全ての問題Formリスト
     */
    @Override
    public List<TaskForm> findAll() {
        List<TaskForm> list = new ArrayList<>();

        for (TaskBean taskBean : taskRepository.findAll()) {
            TaskForm taskForm = new TaskForm();
            taskForm.setId(String.valueOf(taskBean.getId()));
            taskForm.setTitle(taskBean.getTitle());
            taskForm.setDescription(taskBean.getDescription());
            list.add(taskForm);
        }

        return list;
    }
    
    /**
     * 課題情報を取得する.
     * @return 課題Form
     */
    @Override
    public TaskForm findById(String id) {
        
        TaskForm taskForm = new TaskForm();
        Optional<TaskBean> optTask = taskRepository.findById(Long.valueOf(id));
        optTask.ifPresent(taskBean -> {
            taskForm.setId(String.valueOf(taskBean.getId()));
            taskForm.setTitle(taskBean.getTitle());
            taskForm.setDescription(taskBean.getDescription());
        });
        
        return taskForm;
    }
    
    /**
     * 課題削除.
     * @param id 課題ID
     */
    @Override
    public void delete(String id) {
        List<TaskBean> taskBeanList = new ArrayList<>();
        Optional<TaskBean> optTask = taskRepository.findById(Long.valueOf(id));
        optTask.ifPresent(taskBean -> taskBeanList.add(taskBean));
        taskRepository.delete(taskBeanList.get(0));
    }

    /**
     * 課題を保存する.
     * @param form コースForm
     * @param easySaveFlg 簡易保存フラグ(easy save flag)
     * @return 保存済みコースForm
     */
    @Override
    public TaskForm save(TaskForm form, boolean easySaveFlg) {
        
        TaskBean taskBean = new TaskBean();
        // ID、タイトル、説明をBeanに設定する
        String taskId = form.getId();
        if (taskId != null && !taskId.equals("")) {
            taskBean.setId(Long.valueOf(taskId));
        }
        
        taskBean.setTitle(form.getTitle());
        taskBean.setDescription(form.getDescription());
        
        if(!easySaveFlg) {
        
	        // 課題作成者（先生ID）を設定する
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        taskBean.setTeacherId(auth.getName());
	        
	        // 課題、問題中間情報をBeanに設定する
	    	taskBean.clearTaskQuestionBean();
	        List<String> questionCheckedList = form.getQuestionCheckedList();
	        if(questionCheckedList != null) {
	        	int i = 0;
	        	for(String questionId : questionCheckedList) {
	        		
		            TaskQuestionBean taskQuestionBean = new TaskQuestionBean();
	
		            Optional<QuestionBean> optQuestion = questionRepository.findById(Long.valueOf(questionId));
		            optQuestion.ifPresent(questionBean -> {
			            if (taskId != null && !taskId.equals("")) {
			                taskQuestionBean.setTaskId(Long.valueOf(taskId));
			            }
			            taskQuestionBean.setQuestionId(questionBean.getId());
		            });
		            taskQuestionBean.setSeqNumber(Long.valueOf(i++));
	
		            taskBean.addTaskQuestionBean(taskQuestionBean);
	        	}
	        }
	        // 問題数を設定する
	        taskBean.setQuestionSize(Long.valueOf(questionCheckedList.size()));
	        
	        // 提示先情報（ユーザ、課題中間情報）をBeanに設定する
	        taskBean.clearStudentTaskBeans();
	        Set<String> studentIdSet = new HashSet<String>();
	        Set<String> classIdSet = new HashSet<String>();
	        List<String> courseCheckedList = form.getCourseCheckedList();
	        if(courseCheckedList != null) {
	        	for(String courseId : courseCheckedList) {
	        		
	                Optional<CourseBean> optCourse = courseRepository.findById(Long.valueOf(courseId));
		            optCourse.ifPresent(courseBean -> {
		            	// コースに所属するクラスを取得
		                classIdSet.addAll(courseBean.getClassIdList());
		                // コースに所属する学生（クラス所属学生を除く）を取得
		            	studentIdSet.addAll(courseBean.getPartStudentIdList());
		            });
	        	}
	        }
	
	        // 選択したクラスと、選択したコースに所属するクラスを結合
	        classIdSet.addAll(form.getClassCheckedList());
	        
	        // クラスに所属する学生を登録
	        for(String classId : classIdSet) {
	            Optional<ClassBean> optClass = classRepository.findById(Long.valueOf(classId));
	            optClass.ifPresent(classBean -> studentIdSet.addAll(classBean.getUserIdList()));
	        }
		            
	        // コース、クラスに所属する全学生と、選択した全学生を結合
	        studentIdSet.addAll(form.getUserCheckedList());
		            
	        for(String studentId : studentIdSet) {
	        	
	        	StudentTaskBean studentTaskBean = new StudentTaskBean();
	            if (taskId != null && !taskId.equals("")) {
	            	studentTaskBean.setTaskId(Long.valueOf(taskId));
	            }
	            studentTaskBean.setUserId(studentId);
	            
	            taskBean.addStudentTaskBean(studentTaskBean);
	        }
        }
        
        // DBに保存する
        taskBean = taskRepository.save(taskBean);
        
        // BeanのデータをFormにコピーする
        TaskForm resultForm = new TaskForm();
        resultForm.setId(String.valueOf(taskBean.getId()));
        resultForm.setTitle(taskBean.getTitle());
        resultForm.setDescription(taskBean.getDescription());

        return resultForm;
    }
    
    /**
     * 年度ごとの問題を取得する(Get yearly question).
     * @param yearId 年度Id(year id)
     * @return 画面用問題マップ（key:チェックボックスID、value：問題ラベル）
     */
    @Override
    public Map<String, String> findAllQuestionByYearAndTerm(String yearId) {
        
        String year = yearId.substring(0, 3);
        String term = yearId.substring(3, 4);
        Map<String, String> map = convertQuestionMap(questionRepository.findByYearAndTerm(year, term));
        
        return map;
    }

    /**
     * 分野ごとの問題を取得する(Get fiealdly question).
     * @param fieldY 大分類(large field)
     * @param fieldM 中分類(middle field)
     * @param fieldS 小分類(small field)
     * @return 画面用問題マップ（key:チェックボックスID、value：問題ラベル）
     */
    @Override
    public Map<String, String> findAllQuestionByField(String fieldL, String fieldM, String fieldS) {
                	
        List<QuestionBean> questionBeanList = null;

        if(fieldS != null && !"".equals(fieldS)) {
        	// 小分類IDで取得
        	
        	questionBeanList = questionRepository.findByFieldSId(Byte.valueOf(fieldS));
        } else if(fieldM != null && !"".equals(fieldM)) {
        	// 中分類IDで取得
        	
        	questionBeanList = questionRepository.findByFieldMId(Byte.valueOf(fieldM));
        } else if(fieldL != null && !"".equals(fieldL)) {
        	// 大分類IDで取得
        	
        	questionBeanList = questionRepository.findByFieldLId(Byte.valueOf(fieldL));
        } else {
        	
        	questionBeanList = questionRepository.findAll();
        }
    	
        Map<String, String> map = convertQuestionMap(questionBeanList);
        
        return map;
    }

    /**
     * 画面用問題マップ取得
     * @return 画面用問題マップ（key:チェックボックスID、value：問題ラベル）
     */
    @Override
    public Map<String, String> findAllMap() {
    	
    	// 年度で並べ替え（Order by time)
    	List<Order> orders = new ArrayList<Order>();
    	Order orderYear = new Order(Sort.Direction.ASC, "year");
    	orders.add(orderYear);
    	Order orderTerm = new Order(Sort.Direction.DESC, "term");
    	orders.add(orderTerm);
    	Order orderNumber = new Order(Sort.Direction.ASC, "number");
    	orders.add(orderNumber);
    	
    	Map<String, String> map = convertQuestionMap(questionRepository.findAll(Sort.by(orders)));
    	
    	return map;
    }

    /**
     * 全コースMap取得
     * @return 全コースMap
     */
    @Override
    public Map<String, String> findAllCourse() {
    	Map<String, String> map = new HashMap<String, String>();
    	
    	for(CourseBean courseBean : courseRepository.findAll()) {
    		map.put(String.valueOf(courseBean.getId()), courseBean.getName());
    	}
    	
    	return map;
    }
    
    /**
     * 全クラスMap取得
     * @return 全クラスMap
     */
    @Override
    public Map<String, String> findAllClass() {
    	Map<String, String> map = new HashMap<String, String>();
    	
    	for(ClassBean classBean : classRepository.findAll()) {
    		map.put(String.valueOf(classBean.getId()), classBean.getName());
    	}
    	
    	return map;
    }
    
    /**
     * コース所属クラスを除いた全クラスMap取得
     * @param exclusionCourseList 除外コースリスト
     * @return 全クラスMap（該当コース所属クラス除外）
     */
    @Override
    public Map<String, String> findAllClass(List<String> exclusionCourseList) {
    	
        // 全てのクラスの情報をマップに取得
        Map<String, String> classMap = findAllClass();
    	
        if (exclusionCourseList != null) {
        	List<String> exclusionClassLlist = new ArrayList<>();
        	exclusionCourseList.forEach(courseId -> {
				Optional<CourseBean> opt = courseRepository.findById(Long.valueOf(courseId));
                opt.ifPresent(courseBean -> exclusionClassLlist.addAll(courseBean.getClassIdList()));
            });
        	
            // 選択済みクラス所属ユーザを除外
            if (exclusionClassLlist != null && classMap != null) {
            	exclusionClassLlist.forEach(classId -> classMap.remove(classId));
            }
        }
    	
    	return classMap;
    }

    /**
     * 全学生Map取得
     * @return 全クラスMap
     */
    @Override
    public Map<String, String> findAllStudent() {
    	Map<String, String> map = new HashMap<String, String>();
    	
    	for(UserBean userBean : userRepository.findAllStudent()) {
    		map.put(userBean.getId(), userBean.getName());
    	}
    	
    	return map;
    }
    
    /**
     * コース所属クラスを除いた全クラスMap取得
     * @param exclutionCourseList 除外コースリスト
     * @param exclutionClassList 除外クラスリスト
     * @return 全クラスMap
     */
    @Override
    public Map<String, String> findAllStudent(List<String> exclusionCourseList, List<String> exclusionClassList) {
    	
        // 全てのユーザの情報をマップに取得
        Map<String, String> userMap = findAllStudent();

        // 除外クラスリスト
    	List<String> cashExclusionClassList = new ArrayList<>();

        if (exclusionCourseList != null) {
        	exclusionCourseList.forEach(courseId -> {
				Optional<CourseBean> opt = courseRepository.findById(Long.valueOf(courseId));
                opt.ifPresent(courseBean -> cashExclusionClassList.addAll(courseBean.getClassIdList()));
            });
        }
        
        // コースに所属する除外クラスと、指定された除外クラスを結合
        cashExclusionClassList.addAll(exclusionClassList);

        // 除外クラスに所属するユーザを取得し、全ユーザ（学生）から除外する
        if (cashExclusionClassList != null) {
            List<String> removeUserLlist = new ArrayList<>();
            cashExclusionClassList.forEach(classId -> {
                Optional<ClassBean> opt = classRepository.findById(Long.valueOf(classId));
                opt.ifPresent(classBean -> removeUserLlist.addAll(classBean.getUserIdList()));
            });
            // クラス所属ユーザを除外する
            if (removeUserLlist != null && userMap != null) {
                removeUserLlist.forEach(userId -> userMap.remove(userId));
            }
        }
    	
    	return userMap;
    }
    
    /**
     * 課題提出状況リストを取得する
     * @param taskId 課題ID(task id)
     * @return 課題提出状況リスト
     */
    @Override
    public List<TaskSubmissionForm> getAnswerdList(String taskId) {
    	List<TaskSubmissionForm> taskSubmissionFormList = new ArrayList<>();
    	Map<String, Boolean> targetStudentAnsweredMap = new HashMap<>();
    	// 課題の問題数を取得する
    	List<Long> questionSizeList = new ArrayList<>();;
    	Optional<TaskBean> optTask = taskRepository.findById(Long.valueOf(taskId));
    	optTask.ifPresent(taskBean -> {
    		questionSizeList.add(taskBean.getQuestionSize());
    		targetStudentAnsweredMap.putAll(taskBean.getStudentAnsweredMap());
    	});
    	
    	// 学生ごとの回答数を取得する
    	Map<String, Integer> stqhMap = new HashMap<>();
    	List<StudentTaskQuestionHistoryBean> stqhBeanList = studentTaskQuestionHistoryRepository.findAllByTaskId(Long.valueOf(taskId));
    	for(StudentTaskQuestionHistoryBean stqhBean : stqhBeanList) {
    		if(stqhBean.getAnswer() != null) {
	    		String userId = stqhBean.getUserId();
	    		if(stqhMap.containsKey(userId)) {
	    			stqhMap.put(userId, stqhMap.get(userId) + 1);
	    		} else {
	    			stqhMap.put(userId, 1);
	    		}
    		}
    	}

    	// 提出状況を作成し、リストに格納する
    	for(Map.Entry<String, Boolean> entry : targetStudentAnsweredMap.entrySet()) {
        	TaskSubmissionForm form = new TaskSubmissionForm();
        	if(stqhMap.containsKey(entry.getKey())) {
        		form.setAnsweredCnt(String.valueOf(stqhMap.get(entry.getKey())));
        	} else {
        		form.setAnsweredCnt("0");
        	}
        	form.setAnsweredFlg(entry.getValue());
        	form.setQuestionCnt(String.valueOf(questionSizeList.get(0)));
        	form.setUserId(entry.getKey());
        	taskSubmissionFormList.add(form);
    	}
    	return taskSubmissionFormList;
    }
    
    /**
     * ドロップダウン項目設定(Set dropdown param).
     * @param form 課題Form(task form)
     * @param model モデル(model)
     */
    @Override
    public void setSelectData(TaskForm form, Model model) {
    	
    	// 年度取得
        Map<String, String> yearMap = findAllYearMap();
        model.addAttribute("yearDropItems", yearMap);
    	
    	// 大分類取得
        Map<String, String> fieldLMap = findAllFieldLMap();
        model.addAttribute("fieldLDropItemsItems", fieldLMap);
    	
    	// 中分類取得
        Map<String, String> fieldMMap = findAllFieldMMap(form.getSelectFieldL());
        model.addAttribute("fieldMDropItems", fieldMMap);
    	
    	// 小分類取得
        Map<String, String> fieldSMap = findAllFieldSMap(form.getSelectFieldM());
        model.addAttribute("fieldSDropItems", fieldSMap);
    	
    }
    
    /**
     * 画面用年度マップ取得
     * @return 画面用年度マップ（key:ドロップダウンリストID、value：年度ラベル）
     */
    private Map<String, String> findAllYearMap() {
    	
    	Map<String, String> map = new LinkedHashMap<String, String>();
    	
    	for(QuestionBean questionBean : questionRepository.findDistinctYearAndTerm()) {
    		StringBuffer keyBuff = new StringBuffer();
    		StringBuffer valueBuff = new StringBuffer();
    		// 年度
    		keyBuff.append(questionBean.getYear());
    		
    		int yearInt = Integer.valueOf(questionBean.getYear());
    		String termStr = questionBean.getTerm();
        	if(yearInt < 2019) {
        		valueBuff.append("平成");
        		valueBuff.append(yearInt - 1988 + "年");
        	} else if(yearInt == 2019) {
        		if("H".equals(termStr)) {
        			valueBuff.append("平成");
        			valueBuff.append(yearInt - 1988 + "年");
        		} else if("A".equals(termStr)) {
        			valueBuff.append("令和元年");
        		}
        	} else if(yearInt > 2020) {
        		valueBuff.append("令和");
        		valueBuff.append(yearInt - 2019 + "年");
        	}
    		// 期
    		if("H".equals(termStr)) {
    			keyBuff.append("H");
    			valueBuff.append("春");
    		} else {
    			keyBuff.append("A");
    			valueBuff.append("秋");
    		}
   			
   			map.put(keyBuff.toString(), valueBuff.toString());
    	}
    	return map;
    }
    
    /**
     * 画面用大分類マップ取得(Get large  map for screen).
     * @return 画面用大分類マップ（key:ドロップダウンリストID、value：大分類ラベル）
     */
    private Map<String, String> findAllFieldLMap() {
    	
    	Map<String, String> map = new LinkedHashMap<String, String>();

    	EnumSet.allOf(FieldLarge.class)
    	  .forEach(fieldL -> map.put(String.valueOf(fieldL.getId()), fieldL.getName()));
    	
    	return map;
    }
    
    /**
     * 画面用中分類マップ取得(Get middle filed map for screen).
     * @param parentId 大分類ID(large field id)
     * @return 画面用中分類マップ（key:ドロップダウンリストID、value：中分類ラベル）
     */
    private Map<String, String> findAllFieldMMap(String parentId) {

    	Map<String, String> map = new LinkedHashMap<String, String>();
    	if(parentId != null && !parentId.equals("")) {
    		map.putAll(FieldMiddle.getMap(Byte.valueOf(parentId)));
    	}
    	return map;
    }
    
    /**
     * 画面用小分類マップ取得(Get small filed map for screen).
     * @param parentId 中分類ID(middle field id)
     * @return 画面用小分類マップ（key:ドロップダウンリストID、value：小分類ラベル）
     */
    private Map<String, String> findAllFieldSMap(String parentId) {
    	
    	
    	Map<String, String> map = new LinkedHashMap<String, String>();
    	if(parentId != null && !parentId.equals("")) {
    		map.putAll(FieldSmall.getMap(Byte.valueOf(parentId)));
    	}
    	return map;
    }

    
    /**
     * 問題Beanリストを画面用Mapに変換(convert question bean to map for monitor).
     * @param questionBeanList 問題Beanリスト(question baen list)
     * @return 画面用問題Map(question map for monitor)
     */
    private Map<String, String> convertQuestionMap(List<QuestionBean> questionBeanList) {
    	Map<String, String> map = new LinkedHashMap<String, String>();
    	
    	for(QuestionBean questionBean : questionBeanList) {
    		StringBuffer valueBuff = new StringBuffer();
    		// 年度
    		int yearInt = Integer.valueOf(questionBean.getYear());
    		String termStr = questionBean.getTerm();
        	if(yearInt < 2019) {
        		valueBuff.append("平成");
        		valueBuff.append(yearInt - 1988 + "年");
        	} else if(yearInt == 2019) {
        		if("H".equals(termStr)) {
        			valueBuff.append("平成");
        			valueBuff.append(yearInt - 1988 + "年");
        		} else if("A".equals(termStr)) {
        			valueBuff.append("令和元年");
        		}
        	} else if(yearInt > 2020) {
        		valueBuff.append("令和");
        		valueBuff.append(yearInt - 2019 + "年");
        	}
    		// 期
    		if("H".equals(questionBean.getTerm())) {
    			valueBuff.append("春");
    		} else {
    			valueBuff.append("秋");
    		}
    		// 問番
   			valueBuff.append("問" + questionBean.getNumber());
   			
   			// 大分類名称
   			valueBuff.append(" - " + FieldLarge.getName("AP", questionBean.getFieldLId()) + " / ");
   			
   			// 中分類名称
   			valueBuff.append(FieldMiddle.getName("AP", questionBean.getFieldMId()) + " / ");
   			
   			// 小分類名称
   			valueBuff.append(FieldSmall.getName("AP", questionBean.getFieldSId()));
   			
   			map.put(String.valueOf(questionBean.getId())/*keyBuff.toString()*/, valueBuff.toString());
    	}

    	return map;
    }
}
