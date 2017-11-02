package doext.implement;

import org.json.JSONObject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import core.DoServiceContainer;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.define.do_AccelerometerSensor_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_AccelerometerSensor_IMethod接口方法
 * ； #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_AccelerometerSensor_Model extends DoSingletonModule implements do_AccelerometerSensor_IMethod, SensorEventListener {
	private float x;
	private float y;
	private float z;
	private static final int SPEED_SHRESHOLD = 4500;// 这个值越大需要越大的力气来摇晃手机
	private static final int UPTATE_INTERVAL_TIME = 50;
	private SensorManager sensorManager;
	private Sensor sensor;
	private Context mContext;
	private float lastX;
	private float lastY;
	private float lastZ;
	private long lastUpdateTime;

	public do_AccelerometerSensor_Model() throws Exception {
		super();
		mContext = DoServiceContainer.getPageViewFactory().getAppContext();
		sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager != null) {
			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("getAccelerometerData".equals(_methodName)) {
			getAccelerometerData(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("start".equals(_methodName)) {
			start(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("stop".equals(_methodName)) {
			stop(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	public void onSensorChanged(SensorEvent event) {
		long currentUpdateTime = System.currentTimeMillis();
		long timeInterval = currentUpdateTime - lastUpdateTime;
		if (timeInterval < UPTATE_INTERVAL_TIME)
			return;
		lastUpdateTime = currentUpdateTime;
		x = event.values[0];
		y = event.values[1];
		z = event.values[2];
		float deltaX = x - lastX;
		float deltaY = y - lastY;
		float deltaZ = z - lastZ;
		lastX = x;
		lastY = y;
		lastZ = z;
		try {
			DoInvokeResult _result = new DoInvokeResult(getUniqueKey());
			JSONObject _obj = new JSONObject();
			_obj.put("x", x);
			_obj.put("y", y);
			_obj.put("z", z);
			_result.setResultNode(_obj);
			// 触发change事件
			getEventCenter().fireEvent("change", _result);
		} catch (Exception e) {
		}
		double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / timeInterval * 10000;
		if (speed >= SPEED_SHRESHOLD) {
			// 触发shake事件
			getEventCenter().fireEvent("shake", new DoInvokeResult(getUniqueKey()));
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void dispose() {
		sensorManager.unregisterListener(this);
		super.dispose();
	}

	@Override
	public void getAccelerometerData(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {

		try {
			JSONObject _result = new JSONObject();
			_result.put("x", x);
			_result.put("y", y);
			_result.put("z", z);
			_invokeResult.setResultNode(_result);
		} catch (Exception e) {
			DoServiceContainer.getLogEngine().writeError("do_AccelerometerSensor_Model  \n\t", e);
		}
	}

	@Override
	public void start(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if (sensor != null) {
			sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
		}

	}

	@Override
	public void stop(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		sensorManager.unregisterListener(this);
	}
}