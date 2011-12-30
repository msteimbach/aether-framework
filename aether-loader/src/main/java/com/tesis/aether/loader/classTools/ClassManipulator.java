package com.tesis.aether.loader.classTools;

import org.apache.log4j.Logger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

public class ClassManipulator {
	/**
	 * Logger utilizado por la clase
	 */
	private static Logger logger = Logger.getLogger("default");

	/**
	 * Retorna el nombre de la clase pasada como parametro, quitandole el nombre
	 * de paquete
	 * 
	 * @param srcClass
	 *            nombre completo de la clase
	 * @return s�lo el nombre de la clase (quitando el nombre de paquete)
	 */
	private static String getClassName(String srcClass) {
		String className = "";
		if (srcClass != null) {
			String[] names = srcClass.split("\\.");
			if (names.length == 1) {
				return names[0];
			}
			className = names[names.length - 1];
		}
		return className;
	}

	/**
	 * Retorna el nombre del paquete de una clase completa pasada como parametro
	 * 
	 * @param srcClass
	 *            nombre completo de la clase (incluyendo el nombre de paquete)
	 * @return nombre del paquete correspondiente a la clase
	 */
	private static String getPackageName(String srcClass) {
		String pckName = "";
		if (srcClass != null) {
			String[] names = srcClass.split("\\.");
			if (names.length == 1) {
				return "";
			}
			pckName = names[0];
			for (int i = 1; i < names.length - 1; i++) {
				pckName += "." + names[i];
			}
		}
		return pckName;
	}

	/**
	 * Agrega un atributo a la clase especificada.
	 * 
	 * @param _private
	 *            true si el atributo es privado, false si es publico
	 * @param _type
	 *            tipo del atributo (por ejempli String)
	 * @param _fieldName
	 *            nombre del atributo
	 * @param _initialization
	 *            inicializacion de la variable (Ej. = new String())
	 * @param _class
	 *            clase sobre a la cual se le agregara el atributo
	 * @throws CannotCompileException
	 *             en caso de no poder compilarse la clase se lanza la excepcion
	 */
	public static void addClassField(boolean _private, String _type, String _fieldName, String _initialization, CtClass _class) throws CannotCompileException {
		String declaration = (_private) ? "private " : "public ";
		declaration += _type + " " + _fieldName + _initialization + ";";
		CtField f = CtField.make(declaration, _class);
		_class.addField(f);
	}

	/**
	 * Genera un nombre de variable agregando un encabezado y pie para evitar
	 * duplicidad de nombres.
	 * 
	 * @param name
	 *            nombre de variable a generar
	 * @return retorna el nombre de variable generado
	 */
	private static String getGeneratedFieldName(String name) {
		return "_aether_" + name + "_fld";
	}

	/**
	 * Agrega c�digo al metodo pasado por parametro para invocar a otro
	 * correspondiente a la clase indicada.
	 * 
	 * @param className
	 *            nombre de la clase que contiene el m�todo nuevo
	 * @param methodName
	 *            nombre del m�todo de la nueva clase
	 * @param retType
	 *            true si la llamada debe retornar un valor
	 *            false si la llamada no retorna un valor o es un constructor
	 * @param _method
	 *            m�todo al cual agregar la llamada
	 * @param useField
	 *            true si se debe utilizar una variable para realizar la llamada
	 *            al m�todo
	 * @throws CannotCompileException
	 *             en caso de no poder compilarse la clase se lanza la excepcion
	 */
	public static void addCall(String className, String methodName, boolean retType, CtBehavior _method, boolean useField, boolean isConstructor) throws CannotCompileException {
		String _accessName = "";
		if (useField) {
			_accessName = getGeneratedFieldName(getClassName(className));
		} else {
			_accessName = className;
		}
		String _call = "{ ";
		if (useField) {
			// si el atributo es nulo debemos instanciarlo
			_call += "if (" + _accessName + " == null) " + _accessName + " = new " + className + "(); ";
		}
		if (retType) {
			_call += "System.out.println(\"Invoking replaced method: " + methodName + "\");";
			_call += "return " + _accessName + ".getInstance()." + methodName + "($$);}";
		} else {
			if (!isConstructor) {
				_call += "System.out.println(\"Invoking replaced method: " + methodName + "\");";
				_call += _accessName + ".getInstance()." + methodName + "($$); return;}";
			} else { // en este punto seria un constructor
				_call += "System.out.println(\"Invoking added method on constructor: " + methodName + "\");";
				_call += _accessName + ".getInstance()." + methodName + "($$);}";
			}
		}
		if (isConstructor) 
			_method.insertAfter(_call, true);
		else
			_method.setBody(_call);
	}

	/**
	 * Agrega c�digo java en el m�todo especificado
	 * 
	 * @param _after
	 *            true si el c�digo debe intruducirse al final del c�digo
	 *            existente en el m�todo.
	 * @param _method
	 *            m�todo al cual se debe agregar el c�digo indicado
	 * @param _code
	 *            c�digo que se agregar� al m�todo
	 * @throws CannotCompileException
	 *             en caso de no poder compilarse la clase se lanza la excepcion
	 */
	public static void addSpecificCode(boolean _after, CtMethod _method, String _code) throws CannotCompileException {
		if (_after) {
			_method.insertAfter(_code);
		} else {
			_method.insertBefore(_code);
		}
	}

	/**
	 * Agrega las invocaciones a los metodos de las clases especificadas
	 * 
	 * @param origName
	 *            Nombre de la clase original, incluyendo el nombre de paquete
	 * @param nameClassDst
	 *            nombre de la clase destino, a la cual haran referencia las
	 *            llamadas
	 * @param useField
	 *            indica si es necesario utilizar una variable dentro de la
	 *            clase para realizar las invocacoines por medio de ella
	 * @return retorna la clase modificada (con las llamadas correspondientes)
	 * @throws CannotCompileException
	 *             en caso de no poderse compilar la clase
	 * @throws NotFoundException
	 *             en caso de no encontrarse la clase en el pool de clases
	 */
	@SuppressWarnings("rawtypes")
	public static Class addClassCalls(String origName, String nameClassDst, boolean useField) throws CannotCompileException, NotFoundException {
		System.out.println("\nModifying methods for class: " + origName);
		logger.info("Modifying methods for class: " + origName);
		
		ClassPool pool = ClassPool.getDefault();
		String pckName = getPackageName(origName);
		if (!"".equals(pckName)) {
			pool.importPackage(pckName);
		}
		CtClass cc = pool.get(origName);
		CtClass cc2 = pool.get(nameClassDst);
		if (useField) {
			// addClassField(true, nameClassDst,
			// getGeneratedFieldName(getClassName(nameClassDst)), "", cc);
		}
		CtMethod[] methods2 = cc2.getDeclaredMethods();
		int i = 0;
		while (i < methods2.length) {
			CtMethod method2 = methods2[i];

			try {
				CtMethod method = cc.getMethod(method2.getName(), method2.getSignature());
				//if(method.getDeclaringClass().isFrozen()) {
				//	method.getDeclaringClass().defrost();
				//}
				if (!method.toString().contains(" abstract ")) {
					ClassManipulator.addCall(nameClassDst, method.getName(), !method.getReturnType().getName().equals("void"), method, false, false);
					System.out.println("Agregada la llamada en el metodo: " + method.getName());
					logger.info("Agregada la llamada en el metodo: " + method.getName());
				} else {
					System.out.println("No se modificara el metodo abstracto: " + method.getName());
					logger.info("No se modificara el metodo abstracto: " + method.getName());
				}
			} catch (Exception e) {
				System.out.println("No se pudo agregar la llamada en el metodo: '" + method2.getName() + "' posiblemente no exista en la clase origen: '" + origName + "'");
				logger.error("No se pudo agregar c�digo en el m�todo: " + method2.getName(), e);
			}
			i++;

		}
		CtConstructor[] constructors = cc.getConstructors();
		i = 0;
		while (i < constructors.length) {
			CtConstructor constructor = constructors[i];

			try {
				ClassManipulator.addCall(nameClassDst, constructor.getName(), false, constructor, false, true);
				System.out.println("Agregada la llamada en el constructor: '" + constructor.getName());
				logger.info("Agregada la llamada en el constructor: " + constructor.getName());
			} catch (Exception e) {
				System.out.println("No se pudo agregar la llamada en el constructor: '" + constructor.getName() + "' - signatura: " + constructor.getSignature() + ". posiblemente no exista en la clase destino: '" + nameClassDst + "'");
				logger.error("No se pudo agregar c�digo en el m�todo: " + constructor.getName(), e);
			}
			i++;

		}

		return cc.toClass();
	}
}