package com.iii.hmh.midtermproject2;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class ProcessAQIDAO
 */
@WebServlet("/AQI.do")
public class ProcessAQIDAO extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//���o�]�w��ƪ��s�X
		request.setCharacterEncoding("UTF-8");
		//�إ��x�sErrorMessage��MAP(��KEL�I�s
		Map<String,String> errorMessage = new HashMap<String,String>();
		//�]�wrMessage�ѧO�r��s�brequest��
		request.setAttribute("errorMSG", errorMessage);

		//�]�w����ܼƪ�l��
		int id=0,aqi=0,pm=0;
		//���o�|�إ\�઺�ѧO�r��(1�s�W2�ק�3�R��4�d��)
		String doaction = request.getParameter("doaction");
		//�r����int��K���
		int todoaction = Integer.parseInt(doaction);
		
		//Siteid���� ���i����(�|�ӥ\�त)�B�n���Ʀr
		String Siteid = request.getParameter("Siteid");
		if(Siteid==null||Siteid.trim().length()==0) {
			errorMessage.put("iderror","�[����ID����ť�");
		}else if(!(ProcessAQIDAO.isNumeric(Siteid))){
			errorMessage.put("iderror","�[����ID�ݬ����");
		}else {
			//�q�L�h�ഫ�����
			id = Integer.parseInt(Siteid);
		}//Siteid���ҵ���
		
		//�����[�����W�� ���o�[�����W��
		String SiteName = request.getParameter("SiteName");
		//�b�s�W�ק襤�ݭn����
		if(todoaction==1||todoaction==2) {
			if(SiteName==null||SiteName.trim().length()==0) {
				errorMessage.put("sitenameerror","�[�����W���i�ť�");
			}else if(SiteName.length()>3 && 1>=SiteName.length()) {
				errorMessage.put("sitenameerror","�[�����W�٬����T����r");
			}
		for(int j = 0;j<=SiteName.length();j++) {
			if(!(SiteName.matches("[\\u4E00-\\u9FA5]+"))){
				errorMessage.put("sitenameerror","�[�����W�ٽп�J����");
			}
			
		}}//�[�����W�����ҵ���
		
		//���o�����W��
		String Country =request.getParameter("Country");
		
		//����AQI ���oAQI��
		String AQI =request.getParameter("AQI");
		//�b�s�W/�ק襤 AQI���ର�s �B�ݬ����
		if(todoaction==1||todoaction==2) {
			if(AQI==null||AQI.trim().length()==0) {
				errorMessage.put("aqierror","AQI�ƭȤ��i�ť�");
			}else if(!( ProcessAQIDAO.isNumeric(AQI) )) {
				errorMessage.put("aqierror","AQI�ݬ����");			
			}else {
				//�ҳq�L�h�ഫ�����
				aqi = Integer.parseInt(AQI);
			}
		}
		
		//���o��J�����A
		String Status = request.getParameter("Status");
		
		//����pm2.5(�s�W�έק�\�ण�i���s�B�ݬ����
		String PMT =request.getParameter("pm25");
		if(todoaction==1||todoaction==2) {
			if(PMT==null||PMT.trim().length()==0 ) {
				errorMessage.put("pmerror","PM2.5���i���s");
			}else if(!(ProcessAQIDAO.isNumeric(PMT))) {
				errorMessage.put("pmerror","PM2.5�ݬ����");
			}else {
				pm =Integer.parseInt(PMT);
			}
		}
		//���ҵo���ɶ�
		String ptime = request.getParameter("ptime");
		java.sql.Timestamp date = null;
		if( ptime!=null&& ptime.trim().length()!=0) {
			try {
			//����ഫ���~
			date = Timestamp.valueOf(ptime);
			ptime = ptime+".0";
			if(!(ptime.equalsIgnoreCase(date.toString()))) {
				errorMessage.put("timeerror","�п�J���T����P�ɶ�");
			}
			}catch(IllegalArgumentException e) {
				errorMessage.put("timeerror","�п�J���T����P�ɶ�");
			}
		}
		
		//�B�z�Ҧ����~ �Y�����~�T���h���^�쭶��
		if(errorMessage.size()!=0) {//errorMessage���F���ܦ����~
			errorMessage.put("allerrors","�Эץ����~");
			//����^�쭶��
			RequestDispatcher rd = request.getRequestDispatcher("insertAQI.jsp");
	    	rd.forward(request, response);
			return;
		}
		
		AQIBean ab = new AQIBean(id,SiteName,Country,aqi,Status,pm,date);
		HttpSession session =request.getSession();
		session.setAttribute("AQIBean", ab);
		AQIDAO daoaqi = new AQIDAOjdbc();
		//�P�_����s�W��k
		if(todoaction==1) {
			try {
				//�I�s�s�W��k�^�Ƿs�W����
				int exam = daoaqi.insert(ab);
				//�s�W���Ƭ�1�s�W���\ ����s�W����
				if(exam==1) {
				session.setAttribute("AQIbean", ab);
				session.setAttribute("status", "�s�W���");
				response.sendRedirect("success.jsp");
				}else {
					//�s�W���� �ǻ����ѰT���ܴ��J����
					request.setAttribute("statusfail", "�s�W��ƥ���");
					RequestDispatcher rd = request.getRequestDispatcher("insertAQI.jsp");
					rd.forward(request, response);
				}
			} catch (SQLException e) {
				request.setAttribute("statusfail", "�s�W��ƥ���");
				RequestDispatcher rd = request.getRequestDispatcher("insertAQI.jsp");
				rd.forward(request, response);
			}finally {
				try {
					daoaqi.closeConn();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}else if(todoaction==2) { //�P�_����ק��k
			try {
				//�^�ǭקﵧ��
				int exam = daoaqi.update(ab);
				if(exam ==1) {
				session.setAttribute("AQIbean", ab);
				session.setAttribute("status", "�ק���");
				response.sendRedirect("success.jsp");
				}else {
					request.setAttribute("statusfail", "�ק��ƥ���");
					RequestDispatcher rd = request.getRequestDispatcher("insertAQI.jsp");
					rd.forward(request, response);
				}
			} catch (SQLException e) {

				e.printStackTrace();
			}finally {
				try {
					daoaqi.closeConn();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}else if(todoaction==3) {
			try {
				ab = daoaqi.findBySiteid(id);
				int exam = daoaqi.delete(id);
				if(exam==1) {
					session.setAttribute("AQIbean", ab);
					session.setAttribute("status", "�R��");
//					RequestDispatcher rd = request.getRequestDispatcher("success.jsp");
//					rd.forward(request, response);
					response.sendRedirect("success.jsp");
				}else {
					request.setAttribute("statusfail", "�[������ƧR������");
					RequestDispatcher rd = request.getRequestDispatcher("insertAQI.jsp");
					rd.forward(request, response);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				try {
					daoaqi.closeConn();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}else if(todoaction==4) {
			try {
				AQIBean rsbean = daoaqi.findBySiteid(id);
				if(rsbean!=null) {
					session.setAttribute("AQIbean", rsbean);
					session.setAttribute("status", "�d��");
//				RequestDispatcher rd = request.getRequestDispatcher("success.jsp");
//				rd.forward(request, response);	
				response.sendRedirect("success.jsp");
				}else {
					request.setAttribute("statusfail", "�d�L�����");
					RequestDispatcher rd = request.getRequestDispatcher("insertAQI.jsp");
					rd.forward(request, response);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				try {
					daoaqi.closeConn();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	public static boolean isNumeric(String str){
		   for (int i=0;i<str.length();i++){  
		      if (!Character.isDigit(str.charAt(i))){
		    	  return false;
		      }
		   }
		   return true;
		}
}


