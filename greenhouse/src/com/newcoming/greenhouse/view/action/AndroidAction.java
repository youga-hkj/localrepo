package com.newcoming.greenhouse.view.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.newcoming.greenhouse.base.BaseAction;
import com.newcoming.greenhouse.domain.Equipment;
import com.newcoming.greenhouse.domain.Greenhouse;
import com.newcoming.greenhouse.domain.Park;
import com.newcoming.greenhouse.domain.SensorData;
import com.newcoming.greenhouse.domain.User;
import com.newcoming.greenhouse.pojo.GreenhouseForAndroid;
import com.newcoming.greenhouse.pojo.HistorySensorData;
import com.newcoming.greenhouse.pojo.ParkForAndroid;
import com.newcoming.greenhouse.pojo.SensorDataForAndroid;
import com.newcoming.greenhouse.service.SensorDataService;
import com.newcoming.greenhouse.util.AssemblyEquipmentNumber;
import com.newcoming.greenhouse.util.DateUtil;
import com.newcoming.greenhouse.util.TextUtil;

@SuppressWarnings("serial")
@Controller
@Scope("prototype")
public class AndroidAction extends BaseAction<Object> {
	/**
* 
*/
	private static final long serialVersionUID = 1L;

	HttpServletRequest request;
	HttpServletResponse response;

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	// =============== Service实例的声明 ==================

	public void login() {
		try {
			// HttpServletRequest request =ServletActionContext.getRequest();
			// HttpServletResponse response=ServletActionContext.getResponse();
			this.response.setContentType("text/html;charset=utf-8");
			this.response.setCharacterEncoding("UTF-8");
			if (this.request.getParameter("username").equals("123456")) {
				this.response.getWriter().write("真的很奇怪,日本人！");
			} else if (this.request.getParameter("username").equals("zhd")) {
				this.response.getWriter().write("没有错，我就是东子哥！");
			} else {
				this.response.getWriter().write("我就是东子哥！");
			}

			// 将要返回的实体对象进行json处理
			// JSONObject json=JSONObject.fromObject(this.getUsername());
			// 输出格式如：{"id":1, "username":"zhangsan", "pwd":"123"}
			// System.out.println(json);

			// this.response.getWriter().write(json.toString());
			/**
			 * JSONObject json=new JSONObject(); json.put("login", "login");
			 * response.setContentType("text/html;charset=utf-8");
			 * System.out.println(json); byte[] jsonBytes =
			 * json.toString().getBytes("utf-8");
			 * response.setContentLength(jsonBytes.length);
			 * response.getOutputStream().write(jsonBytes);
			 **/
			/**
			 * JSONObject json=new JSONObject(); json.put("login", "login");
			 * byte[] jsonBytes = json.toString().getBytes("utf-8");
			 * response.setContentType("text/html;charset=utf-8");
			 * response.setContentLength(jsonBytes.length);
			 * response.getOutputStream().write(jsonBytes);
			 * response.getOutputStream().flush();
			 * response.getOutputStream().close();
			 **/

		} catch (Exception e) {
			e.printStackTrace();
		}
		// return "login";
	}

	/**
	 * Androird用户登录并返回园区大棚信息的接口
	 */
	public void loginUser() {
		System.out.println("loginUser");

		String username = this.request.getParameter("username");
		String password = this.request.getParameter("password");
		System.out.println(username + "  " + password);
		if ((!TextUtil.isEmpty(username)) && (!TextUtil.isEmpty(password))) {

			// UserServiceImpl userService = new UserServiceImpl();
			User user = userService.findByLoginNameAndPassword(username,
					password);
			System.out.println("usernull");
			if (user != null) {
				// ParkService parkService = new ParkServiceImpl();
				System.out.println("usernotnull");
				/**
				 * 根据用户id查找到这个用户所有的园区
				 */
				List<Park> park = parkService.getAllParks(user.getId());
				/**
				 * 创建json的Parkbean类的列表
				 */
				List<ParkForAndroid> parks = new ArrayList<ParkForAndroid>();
				for (Park p : park) {
					ParkForAndroid parkForAndroid = new ParkForAndroid();
					parkForAndroid.setId(p.getId());
					parkForAndroid.setName(p.getName());
					parkForAndroid.setParkId(p.getParkId());
					List<GreenhouseForAndroid> greenhouses = new ArrayList<GreenhouseForAndroid>();
					Set<Greenhouse> set = p.getGreenhouses();
					Iterator<Greenhouse> iterator = set.iterator();
					while (iterator.hasNext()) {
						Greenhouse g = (Greenhouse) iterator.next();
						GreenhouseForAndroid greenhouseForAndroid = new GreenhouseForAndroid();
						greenhouseForAndroid.setId(g.getId());
						greenhouseForAndroid.setName(g.getName());
						greenhouseForAndroid.setGreenhouseId(g
								.getGreenhouseId());
						greenhouses.add(greenhouseForAndroid);
					}
					parkForAndroid.setGreenhouses(greenhouses);
					parks.add(parkForAndroid);
				}

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("parks", parks);
				JSONObject jsonObject = JSONObject.fromObject(map);
				sendjson(jsonObject);

			} else {
				/**
				 * 0失败，用户不存在
				 */
				fail();
			}
		}

	}

	private void sendjson(JSONObject jsonObject) {
		byte[] jsonBytes;
		try {
			jsonBytes = jsonObject.toString().getBytes("utf-8");
			this.response.setContentType("text/html;charset=utf-8");
			this.response.setCharacterEncoding("UTF-8");
			response.setContentLength(jsonBytes.length);
			response.getOutputStream().write(jsonBytes);
			response.getOutputStream().flush();
			response.getOutputStream().close();
			System.out.println(jsonObject.toString());
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

	private void fail() {
		try {
			/**
			 * 0失败，用户不存在
			 */
			response.getOutputStream().write("0".getBytes());
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 安卓实时数据接口
	 */
	public void realData() {
		/**
		 * parkid,greenhouseid,type,sensor 园区编号，大棚编号，设备类型，传感器类型
		 */
		System.out.println("sensorData");
		String parkid = this.request.getParameter("parkid");

		String greenhouseid = this.request.getParameter("greenhouseid");
		String type = this.request.getParameter("type");
		String sensor = this.request.getParameter("sensor");

		if ((!TextUtil.isEmpty(parkid)) && (!TextUtil.isEmpty(greenhouseid))
				&& (!TextUtil.isEmpty(type)) && (!TextUtil.isEmpty(sensor))) {

			/**
			 * 这里可能会有数字解析异常
			 */
			try {
				Long parknum = Long.parseLong(parkid);
				Long greenhousenum = Long.parseLong(greenhouseid);
				Long typenum = Long.parseLong(type);
				Integer sensornum = Integer.parseInt(sensor);

				String equipmentid = AssemblyEquipmentNumber.equipmentnumber(
						parknum, greenhousenum, typenum, sensornum);

				Equipment equipment = equipmentService
						.findEquipmentByLikeEquipmentId(equipmentid);

				if (equipment != null) {
					if (equipment.getId() != null) {
						SensorData sensorData = sensorDataService.getRealtimeDataByEquipId(equipment
								.getId());
						
						SensorDataForAndroid sensorDataForAndroid = new SensorDataForAndroid();
						sensorDataForAndroid.setTime(DateUtil.toString(sensorData.getTime(), "yyyy-MM-dd HH:mm:ss"));
						sensorDataForAndroid.setValue(sensorData.getValue());
						JSONObject jsonObject = JSONObject.fromObject(sensorDataForAndroid);
						sendjson(jsonObject);
						
						
						
						
					} else {
						fail();
					}

				} else {
					fail();
				}
			} catch (Exception e) {
				fail();
			}
		} else {
			fail();
		}

	}

	/**
	 * 历史数据接口
	 */
	public void historyData() {
		System.out.println("historyData");
		String parkid = this.request.getParameter("parkid");

		String greenhouseid = this.request.getParameter("greenhouseid");
		String type = this.request.getParameter("type");
		String sensor = this.request.getParameter("sensor");

		if ((!TextUtil.isEmpty(parkid)) && (!TextUtil.isEmpty(greenhouseid))
				&& (!TextUtil.isEmpty(type)) && (!TextUtil.isEmpty(sensor))) {

			/**
			 * 这里可能会有数字解析异常
			 */
			try {
				Long parknum = Long.parseLong(parkid);
				Long greenhousenum = Long.parseLong(greenhouseid);
				Long typenum = Long.parseLong(type);
				Integer sensornum = Integer.parseInt(sensor);

				String equipmentid = AssemblyEquipmentNumber.equipmentnumber(
						parknum, greenhousenum, typenum, sensornum);

				Equipment equipment = equipmentService
						.findEquipmentByLikeEquipmentId(equipmentid);

				if (equipment != null) {
					if (equipment.getEquipmentId() != null) {
						List<SensorData> onedaysensordata = sensorDataService.getAllHistorySensorData(equipment.getEquipmentId(), DateUtil.toString(new Date(), "yyyy-MM-dd HH:mm:ss"), 1);
						List<SensorData> threedaysensordata = sensorDataService.getAllHistorySensorData(equipment.getEquipmentId(), DateUtil.toString(new Date(), "yyyy-MM-dd HH:mm:ss"),3);
						List<SensorData> oneweeksensordata = sensorDataService.getAllHistorySensorData(equipment.getEquipmentId(), DateUtil.toString(new Date(), "yyyy-MM-dd HH:mm:ss"), 7);
						
						List<SensorDataForAndroid> oneday = new ArrayList<SensorDataForAndroid>();
						List<SensorDataForAndroid> threeday = new ArrayList<SensorDataForAndroid>();
						List<SensorDataForAndroid> oneweek = new ArrayList<SensorDataForAndroid>();
						
						for (SensorData sensorData: onedaysensordata) {
							SensorDataForAndroid sensorDataForAndroid = new SensorDataForAndroid();
							sensorDataForAndroid.setTime(DateUtil.toString(sensorData.getTime(), "yyyy-MM-dd HH:mm:ss"));
							sensorDataForAndroid.setValue(sensorData.getValue());
							oneday.add(sensorDataForAndroid);
							
						}
						for (SensorData sensorData: threedaysensordata) {
							SensorDataForAndroid sensorDataForAndroid = new SensorDataForAndroid();
							sensorDataForAndroid.setTime(DateUtil.toString(sensorData.getTime(), "yyyy-MM-dd HH:mm:ss"));
							sensorDataForAndroid.setValue(sensorData.getValue());
							threeday.add(sensorDataForAndroid);
							
						}
						for (SensorData sensorData: oneweeksensordata) {
							SensorDataForAndroid sensorDataForAndroid = new SensorDataForAndroid();
							sensorDataForAndroid.setTime(DateUtil.toString(sensorData.getTime(), "yyyy-MM-dd HH:mm:ss"));
							sensorDataForAndroid.setValue(sensorData.getValue());
							oneweek.add(sensorDataForAndroid);
							
						}
						
						
						
						HistorySensorData historySensorData = new HistorySensorData(oneday, threeday, oneweek);
						
						JSONObject jsonObject = JSONObject.fromObject(historySensorData);
						sendjson(jsonObject);
						
						
					} else {
						fail();
					}

				} else {
					fail();
				}
			} catch (Exception e) {
				fail();
			}
		} else {
			fail();
		}

	}

}
