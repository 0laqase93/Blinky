package dam.tfg.blinky.data.repository

import dam.tfg.blinky.data.datasource.UserDataSource
import dam.tfg.blinky.data.mapper.UserMapper
import dam.tfg.blinky.domain.model.User
import dam.tfg.blinky.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of UserRepository that uses a UserDataSource to fetch data.
 */
class UserRepositoryImpl(
    private val userDataSource: UserDataSource
) : UserRepository {

    override suspend fun getUserProfile(): User = withContext(Dispatchers.IO) {
        val userDTO = userDataSource.getUserProfile()
        return@withContext UserMapper.mapToDomain(userDTO)
    }

    override suspend fun updateUserProfile(user: User): Boolean = withContext(Dispatchers.IO) {
        val userDTO = UserMapper.mapToDTO(user)
        return@withContext userDataSource.updateUserProfile(userDTO)
    }

    override suspend fun updateUserName(name: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userDataSource.updateUserName(name)
    }

    override suspend fun verifyPassword(password: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userDataSource.verifyPassword(password)
    }

    override suspend fun resetPassword(newPassword: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userDataSource.resetPassword(newPassword)
    }

    override suspend fun requestPasswordResetEmail(email: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userDataSource.requestPasswordResetEmail(email)
    }

    override suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        return@withContext userDataSource.logout()
    }
}