import io.mockk.ConstantAnswer

/**
 * This function is required for the StubScope extension and taken from the official MockK project.
 *
 * @see <a href="https://github.com/mockk/mockk">Mock</a>
 */
internal fun <T> List<T>.allConst() = this.map { ConstantAnswer(it) }
