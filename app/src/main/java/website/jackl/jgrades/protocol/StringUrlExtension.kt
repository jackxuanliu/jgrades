package website.jackl.jgrades.protocol

/**
 * Created by jack on 12/28/17.
 */

fun String.addUrlPath(path: String): String
{
    return if (this.endsWith("/")) this + path.trim('/') else this + "/" + path.trim('/')
}