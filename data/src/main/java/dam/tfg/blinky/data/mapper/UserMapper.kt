package dam.tfg.blinky.data.mapper

import dam.tfg.blinky.data.dto.UserDTO
import dam.tfg.blinky.domain.model.User

/**
 * Mapper class to convert between User domain model and UserDTO data model.
 */
object UserMapper {
    
    /**
     * Maps a UserDTO to a User domain model.
     */
    fun mapToDomain(userDTO: UserDTO): User {
        return User(
            id = userDTO.id,
            name = userDTO.name,
            email = userDTO.email,
            profilePictureUrl = userDTO.profilePictureUrl
        )
    }
    
    /**
     * Maps a User domain model to a UserDTO.
     */
    fun mapToDTO(user: User): UserDTO {
        return UserDTO(
            id = user.id,
            name = user.name,
            email = user.email,
            profilePictureUrl = user.profilePictureUrl
        )
    }
}