package metamodel.generator

import java.nio.file.Paths
import java.nio.file.Files

class Main {

	static val baseDir = Paths.get("/home/vitali/Public/Projects/AndroidStudioProj/Neo4jEMF/EmfModel/metamodel_test")
	//val baseDir = Paths.get("./model")

	def static void main(String[] args) {

		if(Files.exists(baseDir))
			Files.walk(baseDir).map[it.toFile].forEach[it.delete]
		Files.createDirectories(baseDir)


		//val testList = #[3, 2]
		val longList = #[20, 50, 70, 100, 120, 150, 170, 200, 250, 300, 370, 450, 570, 700]
		val shortList = #[1, 2, 3, 5, 7 /*, 8*/]

		print("long list: ")
		longList.forEach[ n |
		 	genAttrString(n)
		 	genSelfRef(n)

		 	genRef(1, n, 1)
		 	genRef(n, 1, 1)
		 	genRef(1, n, n)

		 	genSimpleSuperTypesHorizontal(n)
		 	genSimpleSuperTypesVertical(n)

		 	print(n + " ")
		]

		print("\nShort list: ")
		shortList.forEach[ n |
			genSimpleSuperTypesRecursive(n)

			print(n + " ")
		]

		println("\nFinished")
	}

	static def genAttrString(int n) {
		val writer = Files.newBufferedWriter(Paths.get('''«baseDir»/Attr_string_«n».ecore'''))
		writer.append('''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
				name="attr_«n»" nsURI="attr_«n»" nsPrefix="attr_«n»">
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
		val writer = Files.newBufferedWriter(Paths.get('''«baseDir»/Ref_«n»_src«src»_trg«tar».ecore'''))
		val artefact = '''"src«src»_ref«n»_tar«tar»"'''

		writer.append('''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
				name=«artefact» nsURI=«artefact» nsPrefix=«artefact»>
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


	static def genSimpleSuperTypesHorizontal(int n) {
		val writer = Files.newBufferedWriter(Paths.get('''«baseDir»/SimpleSyperTypesHorizontal_«n».ecore'''))

		writer.append('''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
				name="simpleSuperTypesHorizontal_«n»"
				nsURI="simpleSuperTypesHorizontal_«n»" nsPrefix="simpleSuperTypesHorizontal_«n»">
				<eClassifiers xsi:type="ecore:EClass" name="Base" />
		''')

		for (i : 1..n) writer.write('''
			<eClassifiers xsi:type="ecore:EClass" name="Class_«i»" eSuperTypes="#//Base" />
		''')

		writer.append("</ecore:EPackage>")
		writer.close
	}

	static def genSimpleSuperTypesVertical(int n) {
		val writer = Files.newBufferedWriter(Paths.get('''«baseDir»/SimpleSyperTypesVertical_«n».ecore'''))

		writer.append('''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
				name="simpleSuperTypesVertical_«n»" nsURI="simpleSuperTypesVertical_«n»"
				nsPrefix="simpleSuperTypesVertical_«n»">
				<eClassifiers xsi:type="ecore:EClass" name="Class_0" />
		''')

		for (i : 1..n) writer.append('''
			<eClassifiers xsi:type="ecore:EClass" name="Class_«i»" eSuperTypes="#//Class_«i-1»" />
		''')

		writer.append("</ecore:EPackage>")
		writer.close
	}


	static def genSimpleSuperTypesRecursive(int n) {
		val writer = Files.newBufferedWriter(Paths.get('''«baseDir»/SimpleSyperTypesRecursion_«n».ecore'''))

		writer.append('''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
				name="simpleSuperTypesRecursive_«n»" nsURI="simpleSuperTypesRecursive_«n»"
				nsPrefix="simpleSuperTypesRecursive_«n»">
				<eClassifiers xsi:type="ecore:EClass" name="Class_0_1_1" />
		''')

		for (i : 1..n) {	//depth
			var g = 1
			var p = 1
			for (j : 1..(new Double(Math.pow(n, i-1)).intValue())) {	//groups
				for (k : 1..n) {
					writer.append('''
						<eClassifiers xsi:type="ecore:EClass" name="Class_«i»_«j»_«k»" eSuperTypes="#//Class_«i-1»_«g»_«p»" />
					''')
				}
				p++
				if (j % n == 0) {g++ p=1}
			}
		}
		writer.append('''</ecore:EPackage>''')
		writer.close
	}

	/*
	static def genAttrMap(int n)'''
	   <?xml version="1.0" encoding="UTF-8"?>
	   <ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	       xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
	       name="Attr_map_«n»" nsURI="Attr_map" nsPrefix="Attr_map">
	     <eClassifiers xsi:type="ecore:EClass" name="Root">
	       «FOR i : 0..n»
	       <eStructuralFeatures xsi:type="ecore:EAttribute" name="map«n»" transient="true">
	         <eGenericType eClassifier="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EMap">
	           <eTypeArguments/>
	           <eTypeArguments/>
	         </eGenericType>
	       </eStructuralFeatures>
	       «ENDFOR»
	     </eClassifiers>
	   </ecore:EPackage>

	'''

	*/
}