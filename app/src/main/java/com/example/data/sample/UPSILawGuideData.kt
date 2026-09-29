package com.example.data.sample

import com.example.data.model.LawArticle
import com.example.data.model.LawCategory

object UPSILawGuideData {
    val articles: List<LawArticle> = listOf(
        LawArticle(
            id = "IPC_302",
            category = LawCategory.IPC,
            titleHindi = "धारा 302: हत्या के लिए दण्ड (Punishment for Murder)",
            titleEnglish = "Section 302: Punishment for Murder",
            keyPointsHindi = "जो कोई हत्या (धारा 300) करेगा, वह मृत्युदण्ड या आजीवन कारावास से दण्डित किया जाएगा और जुर्माने से भी दण्डनीय होगा। यह संज्ञेय, गैर-जमानती और सत्र न्यायालय द्वारा विचारणीय अपराध है।",
            keyPointsEnglish = "Whoever commits murder shall be punished with death, or imprisonment for life, and shall also be liable to fine. Cognizable and non-bailable.",
            punishmentOrProvision = "मृत्युदण्ड या आजीवन कारावास + जुर्माना",
            landmarkCaseOrNote = "बचन सिंह बनाम पंजाब राज्य (1980) - दुर्लभ से दुर्लभतम मामलों (Rarest of Rare Cases) में ही मृत्युदण्ड।"
        ),
        LawArticle(
            id = "IPC_304B",
            category = LawCategory.IPC,
            titleHindi = "धारा 304B: दहेज मृत्यु (Dowry Death)",
            titleEnglish = "Section 304B: Dowry Death",
            keyPointsHindi = "यदि विवाह के 7 वर्ष के भीतर महिला की मृत्यु असामान्य रूप से होती है और मृत्यु से पूर्व पति या उसके नातेदारों द्वारा दहेज के लिए प्रताड़ित किया गया हो।",
            keyPointsEnglish = "Unnatural death of a woman within 7 years of marriage where she was subjected to cruelty for dowry soon before death.",
            punishmentOrProvision = "न्यूनतम 7 वर्ष, जो आजीवन कारावास तक हो सकता है।",
            landmarkCaseOrNote = "भारतीय साक्ष्य अधिनियम की धारा 113B के तहत दहेज मृत्यु की विधिक उपधारणा (Presumption of Dowry Death)।"
        ),
        LawArticle(
            id = "IPC_498A",
            category = LawCategory.IPC,
            titleHindi = "धारा 498A: पति या उसके नातेदारों द्वारा स्त्री के प्रति क्रूरता",
            titleEnglish = "Section 498A: Cruelty by Husband or Relatives",
            keyPointsHindi = "विवाहित महिला को आत्महत्या के लिए विवश करना या दहेज/संपत्ति के लिए शारीरिक या मानसिक रूप से गंभीर रूप से प्रताड़ित करना।",
            keyPointsEnglish = "Subjecting a married woman to physical or mental cruelty or harassment to coerce unlawful dowry demand.",
            punishmentOrProvision = "3 वर्ष तक का कारावास तथा जुर्माना",
            landmarkCaseOrNote = "अरणेश कुमार बनाम बिहार राज्य (2014) - 7 वर्ष से कम सजा वाले अपराधों में धारा 41 CrPC के तहत गिरफ्तारी दिशा-निर्देश।"
        ),
        LawArticle(
            id = "IPC_96_106",
            category = LawCategory.IPC,
            titleHindi = "धारा 96 से 106: प्राइवेट प्रतिरक्षा का अधिकार (Right of Private Defence)",
            titleEnglish = "Sections 96-106: Right of Private Defence",
            keyPointsHindi = "कोई बात अपराध नहीं है जो प्राइवेट प्रतिरक्षा के अधिकार के प्रयोग में की जाए। धारा 100 में शरीर की रक्षा में मृत्यु कारित करने की परिस्थितियां हैं (बलात्कार का भय, अप्राकृतिक कामवासना, अपहरण आदि)।",
            keyPointsEnglish = "Nothing is an offence done in exercise of the right of private defence. Section 100 lists situations where death can be caused in self-defence.",
            punishmentOrProvision = "सुरक्षात्मक अधिकार (प्रतिरक्षा)",
            landmarkCaseOrNote = "प्राइवेट प्रतिरक्षा का अधिकार आक्रामकता का अधिकार नहीं, बल्कि आत्म-सुरक्षा की ढाल है।"
        ),
        LawArticle(
            id = "CRPC_41",
            category = LawCategory.IPC,
            titleHindi = "धारा 41 CrPC: पुलिस कब वारंट के बिना गिरफ्तार कर सकेगी",
            titleEnglish = "Section 41 CrPC: When Police may arrest without warrant",
            keyPointsHindi = "संज्ञेय अपराध करने वाले, भगोड़े अपराधी, चोरी की संपत्ति रखने वाले, पुलिस कार्य में बाधा डालने वाले को बिना वारंट गिरफ्तार करने की शक्तियां।",
            keyPointsEnglish = "Police powers to arrest without warrant for cognizable offences, proclaimed offenders, stolen property possession.",
            punishmentOrProvision = "विधिक प्रक्रिया एवं गिरफ्तारी ज्ञापन (Arrest Memo) अनिवार्य",
            landmarkCaseOrNote = "डी.के. बसु बनाम पश्चिम बंगाल राज्य (1997) - गिरफ्तारी संबंधी ऐतिहासिक 11 दिशानिर्देश।"
        ),
        LawArticle(
            id = "CRPC_154",
            category = LawCategory.CRPC,
            titleHindi = "धारा 154 CrPC: प्रथम सूचना रिपोर्ट (FIR)",
            titleEnglish = "Section 154 CrPC: Information in Cognizable Cases (FIR)",
            keyPointsHindi = "प्रत्येक संज्ञेय अपराध की सूचना थाने के भारसाधक अधिकारी द्वारा लेखबद्ध की जाएगी और उसकी एक प्रति निःशुल्क सूचनादाता को तुरंत दी जाएगी।",
            keyPointsEnglish = "Every information relating to cognizable offence must be recorded in writing and a free copy given to informant.",
            punishmentOrProvision = "ललिता कुमारी बनाम उत्तर प्रदेश सरकार (2014)",
            landmarkCaseOrNote = "संज्ञेय अपराध की सूचना मिलने पर FIR दर्ज करना पुलिस अधिकारी के लिए अनिवार्य है।"
        ),
        LawArticle(
            id = "CONST_32",
            category = LawCategory.CONSTITUTION,
            titleHindi = "अनुच्छेद 32: संवैधानिक उपचारों का अधिकार",
            titleEnglish = "Article 32: Right to Constitutional Remedies",
            keyPointsHindi = "मौलिक अधिकारों के प्रवर्तन के लिए सर्वोच्च न्यायालय में याचिका दायर करने का अधिकार। 5 रिट: 1. बंदी प्रत्यक्षीकरण 2. परमादेश 3. प्रतिषेध 4. उत्प्रेषण 5. अधिकार-पृच्छा।",
            keyPointsEnglish = "Right to move Supreme Court for enforcement of Fundamental Rights. 5 Writs: Habeas Corpus, Mandamus, Prohibition, Certiorari, Quo-Warranto.",
            punishmentOrProvision = "संविधान की आत्मा और हृदय",
            landmarkCaseOrNote = "अनुच्छेद 226 के तहत यही रिट अधिकार उच्च न्यायालय (High Court) को भी प्राप्त है।"
        ),
        LawArticle(
            id = "CONST_21",
            category = LawCategory.CONSTITUTION,
            titleHindi = "अनुच्छेद 21: प्राण और दैहिक स्वतंत्रता का संरक्षण",
            titleEnglish = "Article 21: Protection of Life and Personal Liberty",
            keyPointsHindi = "किसी व्यक्ति को उसके प्राण या दैहिक स्वतंत्रता से विधि द्वारा स्थापित प्रक्रिया के अनुसार ही वंचित किया जाएगा, अन्यथा नहीं। इसमें निजता का अधिकार, स्वच्छ पर्यावरण, शिक्षा आदि शामिल हैं।",
            keyPointsEnglish = "No person shall be deprived of his life or personal liberty except according to procedure established by law. Includes Right to Privacy.",
            punishmentOrProvision = "मूलभूत मानवाधिकार",
            landmarkCaseOrNote = "मेनका गांधी बनाम भारत संघ (1978) एवं के.एस. पुट्टास्वामी केस (निजता का मौलिक अधिकार)।"
        ),
        LawArticle(
            id = "RTI_2005",
            category = LawCategory.SPECIAL_ACTS,
            titleHindi = "सूचना का अधिकार अधिनियम, 2005 (RTI Act 2005)",
            titleEnglish = "Right to Information Act 2005",
            keyPointsHindi = "सरकारी कामकाज में पारदर्शिता हेतु। सामान्य आवेदन पर 30 दिन में तथा जीवन व स्वतंत्रता के मामले में 48 घंटे में सूचना उपलब्ध कराना अनिवार्य है।",
            keyPointsEnglish = "Promoting transparency in government. Information within 30 days normally, and within 48 hours if related to life or liberty.",
            punishmentOrProvision = "प्रतिदिन ₹250 विलम्ब शुल्क (अधिकतम ₹25,000 जुर्माना)",
            landmarkCaseOrNote = "लोक सूचना अधिकारी (PIO) की कानूनी जवाबदेही।"
        ),
        LawArticle(
            id = "POCSO_2012",
            category = LawCategory.SPECIAL_ACTS,
            titleHindi = "पॉक्सो अधिनियम, 2012 (POCSO Act 2012)",
            titleEnglish = "Protection of Children from Sexual Offences Act 2012",
            keyPointsHindi = "18 वर्ष से कम आयु के बालक/बालिकाओं को यौन अपराधों से संरक्षण। विशेष न्यायालयों (Special Courts) का गठन तथा बाल-मित्रवत (Child-Friendly) प्रक्रिया।",
            keyPointsEnglish = "Protecting children below 18 years from sexual offences. Mandatory reporting, child-friendly courts, stringent penalties.",
            punishmentOrProvision = "कठोर कारावास व मृत्युदण्ड तक का प्रावधान (2019 संशोधन)",
            landmarkCaseOrNote = "अपराध की सूचना पुलिस को न देने पर भी दण्ड का प्रावधान।"
        )
    )
}
