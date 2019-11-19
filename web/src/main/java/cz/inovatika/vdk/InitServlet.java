/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.inovatika.vdk;

import cz.incad.vdkcommon.VDKScheduler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 *
 * @author alberto
 */
public class InitServlet extends HttpServlet {

  public static final Logger LOGGER = Logger.getLogger(InitServlet.class.getName());

  //Directory where cant override configuration  
  public static final String APP_DIR_KEY = "vdk_app_dir";

  //Directory where cant override configuration  
  public static String CONFIG_DIR = ".vdk";

  //Default config directory in webapp
  public static String DEFAULT_CONFIG_DIR = "/assets";

  //Default configuration file 
  public static String DEFAULT_CONFIG_FILE = "config.json";

  //Default config directory in webapp
  public static String DEFAULT_I18N_DIR = "/assets/i18n";

  Scheduler sched;

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
   * methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

  }

  @Override
  public void destroy() {
    try {
      sched.shutdown(false);
    } catch (SchedulerException ex) {
      Logger.getLogger(InitServlet.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void init() throws ServletException {
    try {
      if (getServletContext().getInitParameter("def_config_dir") != null) {
        DEFAULT_CONFIG_DIR = getServletContext().getInitParameter("def_config_dir");
      }

      DEFAULT_CONFIG_FILE = getServletContext().getRealPath(DEFAULT_CONFIG_DIR) + File.separator + DEFAULT_CONFIG_FILE;
      DEFAULT_I18N_DIR = getServletContext().getRealPath(DEFAULT_I18N_DIR);

      if (System.getProperty(APP_DIR_KEY) != null) {
        CONFIG_DIR = System.getProperty(APP_DIR_KEY);
      } else if (getServletContext().getInitParameter("app_dir") != null) {
        CONFIG_DIR = getServletContext().getInitParameter("app_dir");
      } else {
        CONFIG_DIR = System.getProperty("user.home") + File.separator + CONFIG_DIR;
      }
      LOGGER.log(Level.INFO, "app dir is ----> {0}", CONFIG_DIR);
      
      sched = VDKScheduler.getInstance().getScheduler();
      getJobs();
      sched.start();
    } catch (SQLException | SchedulerException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }

  private void getJobs() throws SQLException, SchedulerException {

    try {

      File dir = new File(DEFAULT_CONFIG_DIR + File.separator + "jobs" + File.separator);
      if(!dir.exists()) {
        return;
      }
      File[] children = dir.listFiles();
      for (File child : children) {// check interrupted thread

        if (!child.isDirectory()) {
          VDKScheduler.addJob(child);
        }
      }

    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }

  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }// </editor-fold>

}
