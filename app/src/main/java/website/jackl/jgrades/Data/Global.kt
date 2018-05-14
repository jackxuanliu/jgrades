package website.jackl.jgrades.Data

import website.jackl.data_processor.Data
import website.jackl.jgrades.R

/**
 * Created by jack on 12/17/17.
 */


@Data
data class Global(val theme: Int = R.style.AppTheme_Base, val district: District? = null, val activeEmail: String? = null)