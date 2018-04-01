package website.jackl.jgrades.Data

import website.jackl.data_processor.Data

/**
 * Created by jack on 1/22/18.
 */
@Data data class Student(val info: Info) {
    @Data data class Info(val schoolCode: String, val studentNumber: String, val nameOfStudent: String, val nameOfSchool: String){
        val unique: String
        get() = schoolCode + studentNumber
    }

}