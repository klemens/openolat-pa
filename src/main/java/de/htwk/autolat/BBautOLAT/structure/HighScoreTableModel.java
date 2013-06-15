package de.htwk.autolat.BBautOLAT.structure;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;

import de.htwk.autolat.BBautOLAT.BBautOLATCourseNode;
import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.Student.StudentManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskResult.TaskResult;
import de.htwk.autolat.tools.Scoring.ScoreObject;
import de.htwk.autolat.tools.Scoring.ScoreObjectManagerImpl;
/**
 * 
 * Description:<br>
 * This model shows a table with the following information: <br><br>
 * Name of the Student <br>
 * Matr. Nr. <br>
 * a set of autotool tasks belongs to the structure node with the reached score of each student (with and without scorepoints) and the passed flag. <br>
 * And finally a column shows the sum.
 * 
 * <P>
 * Initial Date:  18.05.2010 <br>
 * @author Joerg
 */
public class HighScoreTableModel extends DefaultTableDataModel implements TableDataModel{
	/**
	 * List of tasks given from a learninggroup or a leaningarea
	 */
	private List<BBautOLATCourseNode> tasks;
	/**
	 * List of toplists associatet to a task
	 */
	private List<List<ScoreObject>> topLists; 
	/**
	 * List of configurations 
	 */
	private List<Configuration> configs;
	/**
	 * Constructor
	 * @param objects
	 * @param tasks
	 */
	public HighScoreTableModel(long courseID, List objects, List<BBautOLATCourseNode> tasks) {
		super(objects);
		
		this.tasks = new ArrayList<BBautOLATCourseNode>();
		this.configs = new ArrayList<Configuration>();
		this.tasks = tasks;
		
		
		//create toplists
		topLists = new ArrayList<List<ScoreObject>>();
		for(BBautOLATCourseNode aTask : this.tasks) {			
			this.configs.add(ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, Long.valueOf(aTask.getIdent())));
			List<ScoreObject> taskTopList = ScoreObjectManagerImpl.getInstance().createCourseNodeToplist(courseID, Long.valueOf(aTask.getIdent()));
			topLists.add(taskTopList);
		}
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return tasks.size() + 4;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity user = (Identity) objects.get(row);
		
		for(int i = 0; i < tasks.size(); i++) {
			if(col == i+3) {
				//return getScore(user, tasks.get(i));
				return getPassed(user, tasks.get(i), i) ? "<span id=\"0" 
						+ getPrefix(getScore(user, tasks.get(i), i)) 
						+ getScore(user, tasks.get(i), i) 
						+ "\"class=\"o_passed\">"
						+ getScore(user, tasks.get(i), i)
						+ " </span>"
						: "<span id=\"0" 
						+ getPrefix(getScore(user, tasks.get(i), i)) 
						+ getScore(user, tasks.get(i), i) 
						+ "\"class=\"o_notpassed\">"
						+ getScore(user, tasks.get(i), i)
						+ " </span>";
			}
		}
		if(col == getColumnCount()) {
			//int result = 0;
			int resultTop = 0;
			int resultPassed = 0;
			for(int i = 0; i < tasks.size(); i++) {
				//result += getPureScore(user, tasks.get(i), i);
				resultTop += getScore(user, tasks.get(i), i);
				if(getPassed(user, tasks.get(i), i)) {
					resultPassed++;
				}
			}
			String prefix = getPrefix(resultTop);
			
			
			return "<font id=\"0" 
						+ prefix 
						+ resultTop
						+ "\">"
						+ resultTop
						+ " / " + resultPassed + " </font>";
		}
		if(col == 0) {
			return user.getName();
		}
		if(col == 1) {
			return user.getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
		}

		return "unknown";
				
	}
	
	private String getPrefix(int value) {
		String summe = String.valueOf(value).trim();
		int length = summe.length();
		String returnValue = "";
		//System.out.println(summe +"::::LÄNGE:" + length);
		for(int i = 0; i < (5 - length); i++){
			returnValue += "0";
		}
		return returnValue;
	}

	private int getScore(Identity user, BBautOLATCourseNode bBautOLATCourseNode, int taskIndex) {
		
		
		List<ScoreObject> topList = topLists.get(taskIndex);
		Student stud = StudentManagerImpl.getInstance().getStudentByIdentity(user);
		
		if(stud == null) {
			return 0;
		}
		
		for(ScoreObject aObject : topList) {
			if(aObject.getIdentity().equals(user)) {
				return aObject.getScorePoints();
			}
		}
		
		return 0;
	}
	
	private boolean getPassed(Identity user, BBautOLATCourseNode bautOLATCourseNode, int taskIndex) {
		
		Student stud = StudentManagerImpl.getInstance().getStudentByIdentity(user);
		Configuration conf = configs.get(taskIndex);
		if(stud == null) {
			return false;
		}
 		
		TaskInstance instance = stud.getTaskInstanceByConfiguration(conf);
		
		if(instance == null) {
			return false;
		}
		
		TaskResult result = instance.getResult();
		
		if(result == null) {
			return false;
		}
		
		return result.getHasPassed();
		
	}
	
	
}
