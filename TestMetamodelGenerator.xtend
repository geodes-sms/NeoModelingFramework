package metamodel.generator

import java.nio.file.Paths
import java.nio.file.Files

class Main {

	//static val baseDir = Paths.get("/home/vitali/Public/Projects/AndroidStudioProj/Neo4jEMF/EmfModel/metamodel_test")
	static val baseDir = Paths.get("./model")

	def static void main(String[] args) {

		if(!Files.exists(baseDir)) Files.createDirectories(baseDir)
		//Files.walk(baseDir).map[it.toFile].forEach[it.delete]

		val testList = #[3, 2]
		val longList = #[20, 50, 70, 100, 120, 150, 170, 200, 250, 300, 370, 450, 540, 600]
		val shortList = #[1, 2, 3, 4, 5]

		print("long list: ")
		testList.forEach[ n |

		 	genAttrString(n)
		 	genSelfRef(n)

		 	genRef(1, n, 1)
		 	genRef(n, 1, 1)
		 	genRef(1, n, n)


		 	genSimpleInheritance(3, n)
		 	genSimpleInheritance(4, n)
		 	genSimpleInheritance(1, n)

		 	print(n + " ")

		]


		println("\nFinished")
	}

	static def genAttrString(int n) {
		val name = '''Attr_str_n«n»'''
		val writer = Files.newBufferedWriter(Paths.get('''«baseDir»/«name».ecore'''))
		writer.append('''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
				name="«name»" nsURI="«name»" nsPrefix="«name»">
				<eClassifiers xsi:type="ecore:EClass" name="Root">
		''')

		for (i : 1..n) writer.append('''
			<eStructuralFeatures xsi:type="ecore:EAttribute" name="a«i
				»" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
		''')

		writer.append('''</eClassifiers></ecore:EPackage>''')
		writer.close
	}

	static def genSelfRef(int n) {
		val writer = Files.newBufferedWriter(Paths.get('''«baseDir»/Ref_«n»_src1_trg0.ecore'''))
		writer.append('''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
				name="selfRef_«n»" nsURI="selfRef_«n»" nsPrefix="selfRef_«n»">
				<eClassifiers xsi:type="ecore:EClass" name="Src">
		''')

		for (i : 1..n) writer.append('''
			<eStructuralFeatures xsi:type="ecore:EReference" name="ref_«i
				»" containment="true" upperBound="-1" eType="#//Src" />
		''')

		writer.append('''</eClassifiers></ecore:EPackage>''')
		writer.close
	}


	/**
	 * src : number of source nodes
	 * n : number refs between each src and target node
	 * targ: number of target nodes
	 */
	static def genRef(int src, int n, int tar) {
		val name = '''Ref_src«src»_n«n»_tar«tar»'''
		val writer = Files.newBufferedWriter(Paths.get('''«baseDir»/«name».ecore'''))

		writer.append('''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
				name="«name»" nsURI="«name»" nsPrefix="«name»">
		''')

		for (i : 1..src) {
			writer.append('''<eClassifiers xsi:type="ecore:EClass" name="Src_«i»">''' + "\n")
			for (j : 1..tar) {
				for (k : 1..n) {
					writer.append('''
						<eStructuralFeatures xsi:type="ecore:EReference" name="ref«k»To«j
							»" containment="true" upperBound="-1" eType="#//Target_«j»"/>
					''')
				}
			}
			writer.append("</eClassifiers>\n")
		}
		for (i : 1..tar) {
			writer.append('''<eClassifiers xsi:type="ecore:EClass" name="Target_«i»"/>''' + "\n")
		}
		writer.append('''</ecore:EPackage>''')
		writer.close
	}


	/*
	 * @param depth is a depth of chain of inheritance
	 * @param width is a number of inherited elements for each element
	 */
	static def genSimpleInheritance(int depth, int width) {
		val name = '''SimpleInheritance_depth«depth»_width«width»'''
		val writer = Files.newBufferedWriter(Paths.get('''«baseDir»/«name».ecore'''))

		writer.append('''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
				name="«name»" nsURI="«name»" nsPrefix="«name»">
				<eClassifiers xsi:type="ecore:EClass" name="Class_0_1_1" />
		''')

		for (i : 1..depth) {
			var g = 1
			var p = 1
			for (j : 1..(new Double(Math.pow(width, i-1)).intValue())) {	//groups
				for (k : 1..width) {
					writer.append('''
						<eClassifiers xsi:type="ecore:EClass" name="Class_«i»_«j»_«k»" eSuperTypes="#//Class_«i-1»_«g»_«p»" />
					''')
				}
				p++
				if (j % width == 0) {g++ p=1}
			}
		}
		writer.append('''</ecore:EPackage>''')
		writer.close
	}

	static def genComplexModel(int attr, int ref, int depth, int width) {
		val name = '''ComplexModel_attr«attr»_depth«depth»_width«width»'''
		val writer = Files.newBufferedWriter(Paths.get('''«baseDir»/«name».ecore'''))

		writer.append('''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
				name="«name»" nsURI="«name»" nsPrefix="«name»">

		''')

		writer.append("")
		writer.close
	}
}