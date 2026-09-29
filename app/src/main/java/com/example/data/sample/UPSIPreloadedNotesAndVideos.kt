package com.example.data.sample

import com.example.data.model.NoteEntity
import com.example.data.model.NoteQuestionCrossRef
import com.example.data.model.Subject
import com.example.data.model.VideoEntity
import com.example.data.model.VideoQuestionCrossRef

object UPSIPreloadedNotesAndVideos {

    val notes = listOf(
        NoteEntity(
            id = "note_mool_vidhi_ipc",
            title = "भारतीय दण्ड संहिता (IPC): मानव शरीर विरुद्ध अपराध (धारा 299-304B)",
            subject = Subject.LAW_CONSTITUTION.displayName,
            topic = "IPC Offences",
            readTimeMin = 15,
            tags = "IPC, Murder, Culpable Homicide, Dowry Death",
            contentMarkdown = """
                # भारतीय दण्ड संहिता 1860: महत्वपूर्ण धाराएं
                
                ### 1. धारा 299: आपराधिक मानव वध (Culpable Homicide)
                - जो कोई मृत्यु कारित करने के आशय से, अथवा ऐसी शारीरिक क्षति पहुंचाने के आशय से जिससे मृत्यु कारित होना संभाव्य है, अथवा यह ज्ञान रखते हुए कि वह उस कार्य से मृत्यु कारित कर सकता है, कोई कार्य करके मृत्यु कारित करता है, वह आपराधिक मानव वध का अपराध करता है।
                
                ### 2. धारा 300: हत्या (Murder)
                - आपराधिक मानव वध हत्या है यदि कार्य मृत्यु कारित करने के आशय से किया गया हो।
                - **अपवाद 1:** गंभीर और अचानक प्रकोपन (Grave and Sudden Provocation)।
                - **अपवाद 2:** शरीर या संपत्ति की निजी प्रतिरक्षा के अधिकार का सद्भावपूर्वक प्रयोग।
                - **अपवाद 3:** लोक सेवक द्वारा विधिपूर्ण शक्ति का प्रयोग करते समय सीमा का अतिक्रमण।
                - **अपवाद 4:** अचानक झगड़ा बिना पूर्वचिन्तन के (Sudden Fight)।
                
                ### 3. धारा 304B: दहेज मृत्यु (Dowry Death)
                - विवाह के **7 वर्ष** के भीतर यदि किसी स्त्री की मृत्यु अप्राकृतिक परिस्थितियों में होती है, और यह दर्शित किया जाता है कि विवाह के पूर्व या पश्चात दहेज की मांग के संबंध में उसके साथ क्रूरता की गई थी।
                - **न्यूनतम दण्ड:** 7 वर्ष का कारावास जो आजीवन कारावास तक हो सकता है।
                - **साक्ष्य अधिनियम धारा 113B:** न्यायालय दहेज मृत्यु की उपधारणा करेगा।
            """.trimIndent()
        ),
        NoteEntity(
            id = "note_crpc_police_procedure",
            title = "दण्ड प्रक्रिया संहिता (CrPC): पुलिस अन्वेषण एवं गिरफ्तारी प्रक्रिया",
            subject = Subject.LAW_CONSTITUTION.displayName,
            topic = "CrPC & Police Powers",
            readTimeMin = 12,
            tags = "CrPC, Arrest, Section 41, Witness",
            contentMarkdown = """
                # दण्ड प्रक्रिया संहिता 1973: पुलिस की शक्तियां
                
                ### 1. धारा 41: पुलिस वारंट के बिना कब गिरफ्तार कर सकेगी
                - संज्ञेय अपराध (Cognizable offence) में जहां अपराध 7 वर्ष या उससे कम कारावास से दण्डनीय है:
                  - गिरफ्तारी तभी आवश्यक है जब फरार होने, साक्ष्य नष्ट करने या गवाह को धमकाने का अंदेशा हो।
                  - **अर्श कुमार बनाम बिहार राज्य (Arnesh Kumar Judgment):** यदि गिरफ्तारी आवश्यक नहीं है, तो धारा 41A के तहत उपस्थित होने की नोटिस जारी करना अनिवार्य है।
                
                ### 2. धारा 161: पुलिस द्वारा साक्षियों की परीक्षा
                - अन्वेषण करने वाला पुलिस अधिकारी मामले के तथ्यों से परिचित किसी भी व्यक्ति की मौखिक परीक्षा कर सकता है।
                - **धारा 162:** पुलिस के समक्ष दिया गया बयान हस्ताक्षरित नहीं कराया जाएगा और इसे केवल धारा 145 साक्ष्य अधिनियम के तहत विरोधाभास साबित करने हेतु प्रयुक्त किया जा सकता है।
                
                ### 3. धारा 154: प्रथम सूचना रिपोर्ट (FIR)
                - संज्ञेय अपराध की सूचना प्राप्त होने पर एफ.आई.आर दर्ज करना पुलिस के लिए बाध्यकारी है (ललिता कुमारी बनाम उत्तर प्रदेश राज्य)।
            """.trimIndent()
        ),
        NoteEntity(
            id = "note_constitution_rights",
            title = "भारतीय संविधान: मूल अधिकार एवं संवैधानिक उपचार (अनुच्छेद 12-35)",
            subject = Subject.LAW_CONSTITUTION.displayName,
            topic = "Constitution",
            readTimeMin = 18,
            tags = "Constitution, Fundamental Rights, Writs, Article 32, Article 21",
            contentMarkdown = """
                # भारतीय संविधान: भाग III मूल अधिकार
                
                ### 1. अनुच्छेद 14: विधि के समक्ष समता
                - राज्य भारत के राज्यक्षेत्र में किसी व्यक्ति को विधि के समक्ष समता से या विधियों के समान संरक्षण से वंचित नहीं करेगा।
                
                ### 2. अनुच्छेद 21: प्राण और दैहिक स्वतंत्रता का संरक्षण
                - विधि द्वारा स्थापित प्रक्रिया के अतिरिक्त किसी व्यक्ति को उसके प्राण या दैहिक स्वतंत्रता से वंचित नहीं किया जाएगा।
                - **मेनका गांधी बनाम भारत संघ (1978):** विधि की प्रक्रिया निष्पक्ष, न्यायपूर्ण और युक्तियुक्त (Just, Fair and Reasonable) होनी चाहिए।
                - इसके अंतर्गत निजता का अधिकार (Right to Privacy - पुट्टास्वामी मामला) भी शामिल है।
                
                ### 3. अनुच्छेद 32: संवैधानिक उपचारों का अधिकार (डॉ. अंबेडकर द्वारा 'संविधान की आत्मा')
                - **बंदी प्रत्यक्षीकरण (Habeas Corpus):** अवैध हिरासत से मुक्ति हेतु।
                - **परमादेश (Mandamus):** लोक कर्तव्य निभाने हेतु प्राधिकारी को आदेश।
                - **प्रतिषेध (Prohibition):** अधीनस्थ न्यायालय को क्षेत्राधिकार अतिक्रमण से रोकने हेतु।
                - **उत्प्रेषण (Certiorari):** अभिलेख मंगाने एवं त्रुटिपूर्ण आदेश निरस्त करने हेतु।
                - **अधिकार पृच्छा (Quo-Warranto):** लोक पद के दावे की वैधता की जांच।
            """.trimIndent()
        ),
        NoteEntity(
            id = "note_hindi_vyakaran",
            title = "सामान्य हिन्दी: रस, छन्द एवं अलंकार पहचान सूत्र",
            subject = Subject.GENERAL_HINDI.displayName,
            topic = "Hindi Grammar & Poetics",
            readTimeMin = 10,
            tags = "Hindi, Vyakaran, Alankar, Rasa",
            contentMarkdown = """
                # सामान्य हिन्दी: काव्य सौंदर्य तत्व
                
                ### 1. अलंकार के भेद
                - **यमक अलंकार:** जब एक ही शब्द दो या दो से अधिक बार आए और हर बार अर्थ भिन्न हो।
                  *उदाहरण:* "कनक कनक ते सौ गुनी मादकता अधिकाय। वा खाये बौराय जग या पाये बौराय॥" (कनक = सोना, धतूरा)।
                - **श्लेष अलंकार:** शब्द एक ही बार आए परन्तु प्रसंग भेद से एक से अधिक अर्थ निकले।
                  *उदाहरण:* "रहिमन पानी राखिये, बिन पानी सब सून।"
                - **अनुप्रास अलंकार:** वर्णों की आवृत्ति बार-बार होना।
                
                ### 2. रस एवं उनके स्थायी भाव
                - **शृंगार रस:** रति (प्रेम)
                - **वीर रस:** उत्साह
                - **करुण रस:** शोक
                - **रौद्र रस:** क्रोध
                - **हास्य रस:** हास
                - **भयानक रस:** भय
            """.trimIndent()
        ),
        NoteEntity(
            id = "note_numerical_aptitude",
            title = "संख्यात्मक योग्यता: चक्रवृद्धि ब्याज एवं कार्य-समय शॉर्टकट",
            subject = Subject.NUMERICAL_ABILITY.displayName,
            topic = "Arithmetic Tricks",
            readTimeMin = 14,
            tags = "Maths, CI/SI, Time and Work, Percentage",
            contentMarkdown = """
                # UP SI संख्यात्मक योग्यता शॉर्ट ट्रिक्स
                
                ### 1. चक्रवृद्धि ब्याज (Compound Interest)
                - 2 वर्ष के लिए CI और SI का अंतर:
                  D = P × (R / 100)²
                - यदि कोई धन चक्रवर्ती ब्याज से n1 वर्ष में m1 गुना होता है, तो m1^k गुना n1 × k वर्षों में होगा।
                
                ### 2. कार्य एवं समय (Time & Work)
                - A किसी कार्य को x दिन तथा B उसे y दिन में करता है, तो दोनों मिलकर:
                  कुल दिन = (x × y) / (x + y)
                - LCM विधि: कुल कार्य = दिनों का LCM। दक्षता = कार्य / दिन।
            """.trimIndent()
        ),
        NoteEntity(
            id = "note_reasoning_aptitude",
            title = "तार्किक परीक्षा: रक्त संबंध एवं न्याय निगमन (Syllogism)",
            subject = Subject.REASONING.displayName,
            topic = "Reasoning Tricks",
            readTimeMin = 12,
            tags = "Reasoning, Blood Relations, Syllogism, Direction",
            contentMarkdown = """
                # UP SI मानसिक अभिरुचि व रीजनिंग
                
                ### 1. न्याय निगमन (Syllogism) - 100/50 नियम
                - सभी (All) = 100 कर्ता, 50 विधेय
                - कुछ (Some) = 50 कर्ता, 50 विधेय
                - कोई नहीं (No) = 100 कर्ता, 100 विधेय
                - दो सकारात्मक (Positive) कथनों से कभी नकारात्मक (Negative) निष्कर्ष नहीं निकलता।
                
                ### 2. रक्त संबंध (Blood Relations)
                - पीढ़ी आरेख (Generation Tree) का प्रयोग करें।
                - पुरुष के लिए (+) तथा महिला के लिए (-) का चिन्ह लगाएं।
                - वैवाहिक संबंध को द्विमुखी तीर (↔) से दर्शाएं।
            """.trimIndent()
        )
    )

    val videos = listOf(
        VideoEntity(
            id = "vid_ipc_complete",
            title = "मूल विधि: IPC महत्वपूर्ण धाराएं - एक मैराथन क्लास",
            subject = Subject.LAW_CONSTITUTION.displayName,
            topic = "IPC Offences",
            durationSec = 2700, // 45 mins
            instructor = "Inspector R.K. Sharma (Law Faculty)",
            description = "धारा 299, 300, 302, 304B, 376, 378, 390 और 420 का संपूर्ण विश्लेषण एवं पिछले वर्षों के प्रश्न।",
            tags = "IPC, Mool Vidhi, UP SI 2026",
            videoUrlOrPath = "https://example.com/videos/upsi_ipc_marathon.mp4"
        ),
        VideoEntity(
            id = "vid_crpc_police",
            title = "CrPC: पुलिस अन्वेषण, धारा 41 नोटिस एवं गिरफ्तारी नियम",
            subject = Subject.LAW_CONSTITUTION.displayName,
            topic = "CrPC & Police Powers",
            durationSec = 1800, // 30 mins
            instructor = "Advocate A. Verma (Ex-DSP)",
            description = "FIR पंजीकरण, धारा 161 बयान, केस डायरी तथा अर्नेश कुमार गाइडलाइन्स की विस्तृत व्याख्या।",
            tags = "CrPC, Arrest, Police Procedure",
            videoUrlOrPath = "https://example.com/videos/upsi_crpc_arrest.mp4"
        ),
        VideoEntity(
            id = "vid_const_writs",
            title = "भारतीय संविधान: अनुच्छेद 32 व 226 की 5 रिट याचिकाएं",
            subject = Subject.LAW_CONSTITUTION.displayName,
            topic = "Constitution",
            durationSec = 1500, // 25 mins
            instructor = "Prof. S. Pandey (Polity Expert)",
            description = "बंदी प्रत्यक्षीकरण, परमादेश, प्रतिषेध, उत्प्रेषण एवं अधिकार पृच्छा के महत्वपूर्ण सुप्रीम कोर्ट केस लॉ।",
            tags = "Polity, Article 32, Writs",
            videoUrlOrPath = "https://example.com/videos/upsi_polity_writs.mp4"
        ),
        VideoEntity(
            id = "vid_hindi_masterclass",
            title = "सामान्य हिन्दी: अलंकार एवं रस पहचानने की सुपरफास्ट ट्रिक",
            subject = Subject.GENERAL_HINDI.displayName,
            topic = "Hindi Grammar & Poetics",
            durationSec = 2100, // 35 mins
            instructor = "Dr. Manisha Gupta (Hindi Academy)",
            description = "यमक, श्लेष, उत्प्रेक्षा, रूपक एवं सभी 9 रसों के स्थायी भाव याद करने की अचूक ट्रिक।",
            tags = "Hindi, Alankar, Rasa, UP Police",
            videoUrlOrPath = "https://example.com/videos/upsi_hindi_alankar.mp4"
        ),
        VideoEntity(
            id = "vid_math_compound_interest",
            title = "गणित: CI & SI अंतर एवं 5 सेकंड में उत्तर निकालने की ट्रिक",
            subject = Subject.NUMERICAL_ABILITY.displayName,
            topic = "Arithmetic Tricks",
            durationSec = 1600, // 26 mins
            instructor = "Er. Vikram Singh (Quant Mentor)",
            description = "UP SI के पिछले 5 वर्षों में पूछे गए चक्रवृद्धि ब्याज के कठिन प्रश्न बिना सूत्र के हल करें।",
            tags = "Math, CI, SI, Calculation Tricks",
            videoUrlOrPath = "https://example.com/videos/upsi_quant_ci.mp4"
        ),
        VideoEntity(
            id = "vid_reasoning_syllogism",
            title = "रीजनिंग: न्याय निगमन (Syllogism) 100/50 मेथड",
            subject = Subject.REASONING.displayName,
            topic = "Reasoning Tricks",
            durationSec = 1800, // 30 mins
            instructor = "Pooja Rajput (Reasoning Head)",
            description = "वेन आरेख की जगह 100-50 नियम से बिना पेन उठाए निष्कर्ष सही या गलत जांचने की विधि।",
            tags = "Reasoning, Syllogism, Logic",
            videoUrlOrPath = "https://example.com/videos/upsi_reasoning_syllogism.mp4"
        )
    )

    // Note <-> Question Cross References
    val noteQuestionLinks = listOf(
        NoteQuestionCrossRef("note_mool_vidhi_ipc", "q_ipc_300"),
        NoteQuestionCrossRef("note_mool_vidhi_ipc", "q_ipc_304b"),
        NoteQuestionCrossRef("note_crpc_police_procedure", "q_crpc_arrest"),
        NoteQuestionCrossRef("note_crpc_police_procedure", "q_crpc_161"),
        NoteQuestionCrossRef("note_constitution_rights", "q_const_21"),
        NoteQuestionCrossRef("note_constitution_rights", "q_const_32"),
        NoteQuestionCrossRef("note_constitution_rights", "q_const_art14"),
        NoteQuestionCrossRef("note_hindi_vyakaran", "q_hindi_sandhi"),
        NoteQuestionCrossRef("note_hindi_vyakaran", "q_hindi_samasa"),
        NoteQuestionCrossRef("note_hindi_vyakaran", "q_hindi_rasa"),
        NoteQuestionCrossRef("note_hindi_vyakaran", "q_hindi_alankar"),
        NoteQuestionCrossRef("note_numerical_aptitude", "q_math_si_ci"),
        NoteQuestionCrossRef("note_numerical_aptitude", "q_math_time_work"),
        NoteQuestionCrossRef("note_numerical_aptitude", "q_math_percentage"),
        NoteQuestionCrossRef("note_reasoning_aptitude", "q_reasoning_blood_rel"),
        NoteQuestionCrossRef("note_reasoning_aptitude", "q_reasoning_syllogism"),
        NoteQuestionCrossRef("note_reasoning_aptitude", "q_reasoning_direction")
    )

    // Video <-> Question Cross References
    val videoQuestionLinks = listOf(
        VideoQuestionCrossRef("vid_ipc_complete", "q_ipc_300"),
        VideoQuestionCrossRef("vid_ipc_complete", "q_ipc_304b"),
        VideoQuestionCrossRef("vid_crpc_police", "q_crpc_arrest"),
        VideoQuestionCrossRef("vid_crpc_police", "q_crpc_161"),
        VideoQuestionCrossRef("vid_const_writs", "q_const_21"),
        VideoQuestionCrossRef("vid_const_writs", "q_const_32"),
        VideoQuestionCrossRef("vid_const_writs", "q_const_art14"),
        VideoQuestionCrossRef("vid_hindi_masterclass", "q_hindi_rasa"),
        VideoQuestionCrossRef("vid_hindi_masterclass", "q_hindi_alankar"),
        VideoQuestionCrossRef("vid_math_compound_interest", "q_math_si_ci"),
        VideoQuestionCrossRef("vid_math_compound_interest", "q_math_percentage"),
        VideoQuestionCrossRef("vid_reasoning_syllogism", "q_reasoning_syllogism"),
        VideoQuestionCrossRef("vid_reasoning_syllogism", "q_reasoning_blood_rel")
    )
}
