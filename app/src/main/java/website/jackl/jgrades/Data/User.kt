package website.jackl.jgrades.Data

import website.jackl.data_processor.Data

/**
 * Created by jack on 12/17/17.
 */
@Data
data class User(var email: String, val password: String? = null, val preferredStudent: Student.Info? = null)

// NOTE: preferredStudent must be set before performing StudentRequiredRequests