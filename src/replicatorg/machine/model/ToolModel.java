/*
  ToolModel.java

  A class to model a toolhead.

  Part of the ReplicatorG project - http://www.replicat.org
  Copyright (c) 2008 Zach Smith

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package replicatorg.machine.model;

import org.w3c.dom.Node;

import replicatorg.app.tools.XML;

public class ToolModel
{
	public static int MOTOR_CLOCKWISE = 1;
	public static int MOTOR_COUNTER_CLOCKWISE = 2;
	
	//our xml config info
	protected Node xml;
	
	//descriptive stuff
	protected String name;
	protected String type;
	protected String material;
	protected int index;

	//motor stuff
	protected boolean motorEnabled;
	protected int motorDirection;
	protected double motorSpeedRPM;
	protected int motorSpeedPWM;
	protected double motorSpeedReadingRPM;
	protected int motorSpeedReadingPWM;
	protected boolean motorHasEncoder;
	protected int motorEncoderPPR;
	protected boolean motorIsStepper;
	protected int motorSteps;

	//spindle stuff
	protected boolean spindleEnabled;
	protected int spindleDirection;
	protected double spindleSpeedRPM;
	protected int spindleSpeedPWM;
	protected double spindleSpeedReadingRPM;
	protected int spindleSpeedReadingPWM;
	protected boolean spindleHasEncoder;
	protected int spindleEncoderPPR;

	//temperature variables
	protected double currentTemperature;
	protected double targetTemperature;

	//platform temperature variables
	protected double platformCurrentTemperature;
	protected double platformTargetTemperature;

	//various coolant/control stuff
	protected boolean floodCoolantEnabled;
	protected boolean mistCoolantEnabled;
	protected boolean fanEnabled;
	protected boolean valveOpen;
	protected boolean colletOpen;
	
	// Z-Probe
	protected int zprobeAngle;
	protected boolean zprobeEngaged;
	
	//capabilities
	protected boolean hasMotor = false;
	protected boolean hasSpindle = false;
	protected boolean hasHeater = false;
	protected boolean hasHeatedPlatform = false;
	protected boolean hasFloodCoolant = false;
	protected boolean hasMistCoolant = false;
	protected boolean hasFan = false;
	protected boolean hasValve = false;
	protected boolean hasCollet = false;
	protected boolean hasZProbe = false;

	/*************************************
	*  Creates the model object.
	*************************************/
	public ToolModel()
	{
		_initialize();
	}
	
	public ToolModel(Node n)
	{
		_initialize();

		//load our XML config
		loadXML(n);
	}
	
	private void _initialize()
	{
		//default information
		name = "Generic Tool";
		type = "tool";
		material = "unknown";
		index = 0;
		
		//default our spindles/motors
		setMotorDirection(MOTOR_CLOCKWISE);
		disableMotor();
		setSpindleDirection(MOTOR_CLOCKWISE);
		disableMotor();
		
		//default our accessories
		disableFloodCoolant();
		disableMistCoolant();
		disableFan();
		closeValve();
		closeCollet();
	}
	
	//load data from xml config
	public void loadXML(Node node)
	{
		xml = node;
		
		//load our name.
		String n = XML.getAttributeValue(xml, "name");
		if (n != null)
			name = n;
			
		//load our type.
		n = XML.getAttributeValue(xml, "type");
		if (n != null)
			type = n;
		
		//load our material
		n = XML.getAttributeValue(xml, "material");
		if (n != null)
			material = n;
		
		//our various capabilities
		n = XML.getAttributeValue(xml, "motor");
		try {
			if (Boolean.parseBoolean(n) || Integer.parseInt(n) == 1)
			{
				hasMotor = true;
				
				n = XML.getAttributeValue(xml, "motor_encoder_ppr");
				try{
					if (Integer.parseInt(n) > 0)
					{
						motorHasEncoder = true;
						motorEncoderPPR = Integer.parseInt(n);
					}
				} catch (Exception e) {} // ignore parse errors.

				n = XML.getAttributeValue(xml, "motor_steps");
				try{
					if (Integer.parseInt(n) > 0)
					{
						motorIsStepper = true;
						motorSteps = Integer.parseInt(n);
					}
				} catch (Exception e) {} // ignore parse errors.

			}
		} catch (Exception e) {} //ignore boolean/integer parse errors

		n = XML.getAttributeValue(xml, "spindle");
		try {
			if (Boolean.parseBoolean(n) || Integer.parseInt(n) == 1)
			{
				hasSpindle = true;
				
				n = XML.getAttributeValue(xml, "motor_encoder_ppr");
				try{
					if (Integer.parseInt(n) > 0)
					{
						motorHasEncoder = true;
						motorEncoderPPR = Integer.parseInt(n);
					}
				} catch (Exception e) {} // ignore parse errors.
			}
		} catch (Exception e) {} //ignore boolean/integer parse errors

		//flood coolant
		n = XML.getAttributeValue(xml, "floodcoolant");
		try {
			if (Boolean.parseBoolean(n) || Integer.parseInt(n) == 1)
				hasFloodCoolant = true;
		} catch (Exception e) {} //ignore boolean/integer parse errors

		n = XML.getAttributeValue(xml, "mistcoolant");
		try {
			if (Boolean.parseBoolean(n) || Integer.parseInt(n) == 1)
				hasMistCoolant = true;
		} catch (Exception e) {} //ignore boolean/integer parse errors

		n = XML.getAttributeValue(xml, "fan");
		try {
			if (Boolean.parseBoolean(n) || Integer.parseInt(n) == 1)
				hasFan = true;
		} catch (Exception e) {} //ignore boolean/integer parse errors

		n = XML.getAttributeValue(xml, "valve");
		try {
			if (Boolean.parseBoolean(n) || Integer.parseInt(n) == 1)
				hasValve = true;
		} catch (Exception e) {} //ignore boolean/integer parse errors

		n = XML.getAttributeValue(xml, "collet");
		try {
			if (Boolean.parseBoolean(n) || Integer.parseInt(n) == 1)
				hasCollet = true;
		} catch (Exception e) {} //ignore boolean/integer parse errors

		n = XML.getAttributeValue(xml, "heater");
		try {
			if (Boolean.parseBoolean(n) || Integer.parseInt(n) == 1)
				hasHeater = true;
		} catch (Exception e) {} //ignore boolean/integer parse errors
		n = XML.getAttributeValue(xml, "heatedplatform");
		try {
			if (Boolean.parseBoolean(n) || Integer.parseInt(n) == 1)
				hasHeatedPlatform = true;
		} catch (Exception e) {} //ignore boolean/integer parse errors

		n = XML.getAttributeValue(xml, "zprobe");
		try {
			if (Boolean.parseBoolean(n) || Integer.parseInt(n) == 1)
				hasZProbe = true;
		} catch (Exception e) {} //ignore boolean/integer parse errors

		//hah, all this for a debug string... lol.
		String result = "Loading " + type + " '" + name + "': ";
		result += "material: " + material + ", ";
		result += "with these capabilities: ";
		if (hasFloodCoolant)
			result += "flood coolant, ";
		if (hasMotor)
			result += "motor, ";
		if (hasSpindle)
			result += "spindle, ";
		if (hasMistCoolant)
			result += "mist coolant, ";
		if (hasFan)
			result += "fan, ";
		if (hasValve)
			result += "valve, ";
		if (hasCollet)
			result += "collet, ";
		if (hasHeater)
			result += "heater, ";
		if (hasHeatedPlatform)
			result += "hasHeatedPlatform, ";
		if (hasZProbe)
			result += "hasZProbe, ";
		//System.out.println(result);
	}
	
	/*************************************
	*  Generic tool information
	*************************************/
	
	public String getName()
	{
		return name;
	}

	public void setIndex(int i)
	{
		index = i;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public String getType()
	{
		return type;
	}

	/*************************************
	*  Motor interface functions
	*************************************/
	public void setMotorDirection(int dir)
	{
		motorDirection = dir;
	}

	public int getMotorDirection()
	{
		return motorDirection;
	}
	
	public void setMotorSpeedRPM(double rpm)
	{
		motorSpeedRPM = rpm;
	}

	public void setMotorSpeedPWM(int pwm)
	{
		motorSpeedPWM = pwm;
	}
	
	public double getMotorSpeedRPM()
	{
		return motorSpeedRPM;
	}

	public int getMotorSpeedPWM()
	{
		return motorSpeedPWM;
	}
	
	public void setMotorSpeedReadingRPM(double rpm)
	{
		motorSpeedReadingRPM = rpm;
	}
	
	public void setMotorSpeedReadingPWM(int pwm)
	{
		motorSpeedReadingPWM = pwm;
	}
	
	public double getMotorSpeedReadingRPM()
	{
		return motorSpeedReadingRPM;
	}

	public int getMotorSpeedReadingPWM()
	{
		return motorSpeedReadingPWM;
	}
	
	public void enableMotor()
	{
		motorEnabled = true;
	}
	
	public void disableMotor()
	{
		motorEnabled = false;
	}
	
	public boolean isMotorEnabled()
	{
		return motorEnabled;
	}
	
	public boolean hasMotor()
	{
		return hasMotor;
	}
	
	public boolean motorHasEncoder()
	{
	  return motorHasEncoder;
	}
	
	public boolean motorIsStepper()
	{
		return motorIsStepper;
	}

	/*************************************
	*  Spindle interface functions
	*************************************/
	public void setSpindleDirection(int dir)
	{
		spindleDirection = dir;
	}

	public int getSpindleDirection()
	{
		return spindleDirection;
	}
	
	public void setSpindleSpeedRPM(double rpm)
	{
		spindleSpeedRPM = rpm;
	}

	public void setSpindleSpeedPWM(int pwm)
	{
		spindleSpeedPWM = pwm;
	}
	
	public double getSpindleSpeedRPM()
	{
		return spindleSpeedRPM;
	}

	public int getSpindleSpeedPWM()
	{
		return spindleSpeedPWM;
	}
	
	public void setSpindleSpeedReadingRPM(double rpm)
	{
		spindleSpeedReadingRPM = rpm;
	}
	
	public void setSpindleSpeedReadingPWM(int pwm)
	{
		spindleSpeedReadingPWM = pwm;
	}
	
	public double getSpindleSpeedReadingRPM()
	{
		return spindleSpeedReadingRPM;
	}

	public int getSpindleSpeedReadingPWM()
	{
		return spindleSpeedReadingPWM;
	}
	
	public void enableSpindle()
	{
		spindleEnabled = true;
	}
	
	public void disableSpindle()
	{
		spindleEnabled = false;
	}
	
	public boolean isSpindleEnabled()
	{
		return spindleEnabled;
	}
	
	public boolean hasSpindle()
	{
		return hasSpindle;
	}
	
	public boolean spindleHasEncoder()
	{
	  return spindleHasEncoder;
	}

	/*************************************
	*  Heater interface functions
	*************************************/
	public void setTargetTemperature(double temperature)
	{
		targetTemperature = temperature;
	}

	public double getTargetTemperature()
	{
		return targetTemperature;
	}

	public void setCurrentTemperature(double temperature)
	{
		currentTemperature = temperature;
	}
	
	public double getCurrentTemperature()
	{
		return currentTemperature;
	}
	
	public boolean hasHeater()
	{
		return hasHeater;
	}

	/*************************************
	*  Heated Platform interface functions
	*************************************/
	public void setPlatformTargetTemperature(double temperature)
	{
		platformTargetTemperature = temperature;
	}

	public double getPlatformTargetTemperature()
	{
		return platformTargetTemperature;
	}

	public void setPlatformCurrentTemperature(double temperature)
	{
		platformCurrentTemperature = temperature;
	}
	
	public double getPlatformCurrentTemperature()
	{
		return platformCurrentTemperature;
	}
	
	public boolean hasHeatedPlatform()
	{
		return hasHeatedPlatform;
	}

	/*************************************
	*  Flood Coolant interface functions
	*************************************/
	public void enableFloodCoolant()
	{
		floodCoolantEnabled = true;
	}
	
	public void disableFloodCoolant()
	{
		floodCoolantEnabled = false;
	}
	
	public boolean isFloodCoolantEnabled()
	{
		return floodCoolantEnabled;
	}
	
	public boolean hasFloodCoolant()
	{
		return hasFloodCoolant;
	}

	/*************************************
	*  Mist Coolant interface functions
	*************************************/
	public void enableMistCoolant()
	{
		mistCoolantEnabled = true;
	}
	
	public void disableMistCoolant()
	{
		mistCoolantEnabled = false;
	}
	
	public boolean isMistCoolantEnabled()
	{
		return mistCoolantEnabled;
	}
	
	public boolean hasMistCoolant()
	{
		return hasMistCoolant;
	}

	/*************************************
	*  Fan interface functions
	*************************************/
	public void enableFan()
	{
		fanEnabled = true;
	}

	public void disableFan()
	{
		fanEnabled = false;
	}
	
	public boolean isFanEnabled()
	{
		return fanEnabled;
	}
	
	public boolean hasFan()
	{
		return hasFan;
	}
	
	/*************************************
	*  Valve interface functions
	*************************************/
	public void openValve()
	{
		valveOpen = true;
	}
	
	public void closeValve()
	{
		valveOpen = false;
	}
	
	public boolean isValveOpen()
	{
		return valveOpen;
	}
	
	public boolean hasValve()
	{
		return hasValve;
	}
	
	/*************************************
	*  Collet interface functions
	*************************************/
	public void openCollet()
	{
		colletOpen = true;
	}
	
	public void closeCollet()
	{
		colletOpen = false;
	}
	
	public boolean isColletOpen()
	{
		return colletOpen;
	}
	
	public boolean hasCollet()
	{
		return hasCollet;
	}
	
	/*************************************
	*  ZProbe interface functions
	*************************************/
	public boolean hasZProbe()
	{
		return hasZProbe;
	}
	
	public void setZProbeAngle(int angle)
	{
		zprobeAngle = angle;
	}
	
	public int getZProbeAngle()
	{
		return zprobeAngle;
	}

	public boolean isZProbeEngaged()
	{
		return zprobeEngaged;
	}
	
	public void engageZProbe()
	{
		zprobeEngaged = true;
	}
	
	public void disengageZProbe()
	{
		zprobeEngaged = false;
	}
	
	/**
	 * Retrieve XML node. A temporary hack until we have more robust tool models.
	 */
	public Node getXml() {
		return xml;
	}
}
