package com.example.palcharity.utils

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

// --------------------- DATA CLASSES ---------------------
data class Donor(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val email: String
)

data class Association(
    val uid: String,
    val name: String,
    val email: String
)

data class Donation(
    val donorId: String,
    val projectId: String,
    val donorFirstName: String,
    val donorLastName: String,
    val typeDonation: String,
    val amount: Int,
    val timestamp: Long,
    val projectTitle: String,
    val associationName: String
)

data class Project(
    val id: String,
    val title: String,
    val description: String,
    val amountNeeded: Int,
    val amountRemaining: Int,
    val associationName: String,
    val type: String,
    val imageUrl: String
)

// --------------------- FIREBASE HELPER ---------------------
class FirebaseHelper {

    private val database = FirebaseDatabase.getInstance().reference

    // ==================== DONOR METHODS ====================

    // 🔹 Récupérer un Donor par UID
    suspend fun getDonorById(uid: String): Donor {
        val snap = database.child("donors").child(uid).get().await()
        return Donor(
            uid = uid,
            firstName = snap.child("firstName").value.toString(),
            lastName = snap.child("lastName").value.toString(),
            email = snap.child("email").value.toString()
        )
    }

    // 🔹 Mettre à jour le profil du Donor
    suspend fun updateDonorProfile(uid: String, firstName: String, lastName: String) {
        database.child("donors").child(uid).updateChildren(
            mapOf(
                "firstName" to firstName,
                "lastName" to lastName
            )
        ).await()
    }

    // 🔹 Statistiques du Donor (nombre de dons + montant total)
    suspend fun getDonorStats(uid: String): Pair<Int, Int> {
        val snap = database.child("don_projet").get().await()
        var count = 0
        var total = 0
        for (d in snap.children) {
            if (d.child("uid").value.toString() == uid) {
                count++
                total += (d.child("amount").value.toString().toIntOrNull() ?: 0)
            }
        }
        return count to total
    }

    // 🔹 Liste des donations d'un Donor
    suspend fun getDonorDonations(uid: String): List<Donation> {
        val snap = database.child("don_projet").get().await()
        val donations = mutableListOf<Donation>()
        for (d in snap.children) {
            if (d.child("uid").value.toString() == uid) {
                donations.add(
                    Donation(
                        donorId = uid,
                        projectId = d.child("projectId").value.toString(),
                        donorFirstName = d.child("donorFirstName").value.toString(),
                        donorLastName = d.child("donorLastName").value.toString(),
                        typeDonation = d.child("type").value.toString(),
                        amount = d.child("amount").value.toString().toIntOrNull() ?: 0,
                        timestamp = d.child("timestamp").value.toString().toLong(),
                        projectTitle = d.child("projectTitle").value.toString(),
                        associationName = d.child("associationName").value.toString()
                    )
                )
            }
        }
        return donations
    }

    // 🔹 Créer un nouveau donor (pour l'inscription)
    suspend fun createDonor(donor: Donor) {
        database.child("donors").child(donor.uid).setValue(
            mapOf(
                "firstName" to donor.firstName,
                "lastName" to donor.lastName,
                "email" to donor.email
            )
        ).await()
    }

    // ==================== ASSOCIATION METHODS ====================

    // 🔹 Récupérer une Association par UID
    // Dans FirebaseHelper.kt - CORRECTION de getAssociationById
    // CORRECTION dans FirebaseHelper.kt
    suspend fun getAssociationById(uid: String): Association {
        val snap = database.child("associations").child(uid).get().await()

        // DEBUG: Afficher la structure des données
        println("🔍 [FIREBASE] Structure des données association:")
        snap.children.forEach { child ->
            println("   ${child.key} = ${child.value}")
        }

        return Association(
            uid = uid,
            name = snap.child("associationName").value.toString(), // ⬅️ Lire associationName
            email = snap.child("email").value.toString()
        )
    }

    // 🔹 Mettre à jour le profil de l'Association
    suspend fun updateAssociationProfile(uid: String, name: String) {
        database.child("associations").child(uid).updateChildren(
            mapOf("name" to name)
        ).await()
    }

    // 🔹 Créer une nouvelle association (pour l'inscription)
    suspend fun createAssociation(association: Association) {
        database.child("associations").child(association.uid).setValue(
            mapOf(
                "name" to association.name,
                "email" to association.email
            )
        ).await()
    }

    // 🔹 Vérifier si une association existe
    suspend fun associationExists(uid: String): Boolean {
        return try {
            val snap = database.child("associations").child(uid).get().await()
            snap.exists()
        } catch (e: Exception) {
            false
        }
    }

    // ==================== PROJECT METHODS ====================

    // 🔹 Récupérer tous les projets avec montant restant > 0
    suspend fun getAllProjects(): List<Project> {
        val snap = database.child("projects").get().await()
        val projects = mutableListOf<Project>()
        for (p in snap.children) {
            val amountRemaining = p.child("amountRemaining").value.toString().toIntOrNull() ?: 0
            if (amountRemaining > 0) {
                projects.add(
                    Project(
                        id = p.child("id").value.toString(),
                        title = p.child("title").value.toString(),
                        description = p.child("description").value.toString(),
                        amountNeeded = p.child("amountNeeded").value.toString().toIntOrNull() ?: 0,
                        amountRemaining = amountRemaining,
                        associationName = p.child("associationName").value.toString(),
                        type = p.child("type").value.toString(),
                        imageUrl = p.child("imageUrl").value.toString()
                    )
                )
            }
        }
        return projects
    }

    // 🔹 Récupérer les projets d'une association spécifique
    suspend fun getAssociationProjects(associationName: String): List<Project> {
        val snap = database.child("projects").get().await()
        val projects = mutableListOf<Project>()
        for (p in snap.children) {
            if (p.child("associationName").value.toString() == associationName) {
                projects.add(
                    Project(
                        id = p.child("id").value.toString(),
                        title = p.child("title").value.toString(),
                        description = p.child("description").value.toString(),
                        amountNeeded = p.child("amountNeeded").value.toString().toIntOrNull() ?: 0,
                        amountRemaining = p.child("amountRemaining").value.toString().toIntOrNull() ?: 0,
                        associationName = p.child("associationName").value.toString(),
                        type = p.child("type").value.toString(),
                        imageUrl = p.child("imageUrl").value.toString()
                    )
                )
            }
        }
        return projects
    }

    // 🔹 Ajouter un nouveau projet
    suspend fun addProject(project: Project) {
        // Créer une nouvelle référence avec push() pour générer un ID unique
        val projectRef = database.child("projects").push()

        // Utiliser l'ID généré par Firebase
        val projectId = projectRef.key ?: System.currentTimeMillis().toString()

        val projectData = mapOf(
            "id" to projectId,
            "title" to project.title,
            "description" to project.description,
            "amountNeeded" to project.amountNeeded,
            "amountRemaining" to project.amountRemaining,
            "associationName" to project.associationName,
            "type" to project.type,
            "imageUrl" to project.imageUrl
        )

        projectRef.setValue(projectData).await()
    }

    // 🔹 Mettre à jour le montant restant d'un projet
    suspend fun updateProjectRemainingAmount(projectId: String, newAmount: Int) {
        database.child("projects").child(projectId).child("amountRemaining")
            .setValue(newAmount).await()
    }

    // ==================== DONATION METHODS ====================

    // 🔹 Ajouter un don
    suspend fun addDonation(
        donorId: String,
        project: Project,
        typeDonation: String,
        amount: Int,
        location: Pair<Double, Double>? = null
    ) {
        val donationRef = database.child("don_projet").push()
        val timestamp = System.currentTimeMillis()

        // Infos du Donor
        val donorSnap = database.child("donors").child(donorId).get().await()
        val donorFirstName = donorSnap.child("firstName").value.toString()
        val donorLastName = donorSnap.child("lastName").value.toString()

        val donationData = mapOf(
            "uid" to donorId,
            "projectId" to project.id,
            "donorFirstName" to donorFirstName,
            "donorLastName" to donorLastName,
            "type" to typeDonation,
            "amount" to amount,
            "timestamp" to timestamp,
            "projectTitle" to project.title,
            "associationName" to project.associationName,
            "location" to location?.let { mapOf("lat" to it.first, "lng" to it.second) }
        )

        donationRef.setValue(donationData).await()

        // Mise à jour du montant restant si c'est un don d'argent
        if (typeDonation == "money") {
            // Trouver le projet par son ID et mettre à jour le montant restant
            val projectsSnap = database.child("projects").get().await()
            for (p in projectsSnap.children) {
                if (p.child("id").value.toString() == project.id) {
                    val currentRemaining = p.child("amountRemaining").value.toString().toIntOrNull() ?: project.amountRemaining
                    val newRemaining = (currentRemaining - amount).coerceAtLeast(0)

                    // Mettre à jour le montant restant
                    database.child("projects").child(p.key!!).child("amountRemaining")
                        .setValue(newRemaining).await()
                    break
                }
            }
        }
    }

    // 🔹 Récupérer tous les dons pour un projet
    suspend fun getProjectDonations(projectId: String): List<Donation> {
        val snap = database.child("don_projet").get().await()
        val donations = mutableListOf<Donation>()
        for (d in snap.children) {
            if (d.child("projectId").value.toString() == projectId) {
                donations.add(
                    Donation(
                        donorId = d.child("uid").value.toString(),
                        projectId = projectId,
                        donorFirstName = d.child("donorFirstName").value.toString(),
                        donorLastName = d.child("donorLastName").value.toString(),
                        typeDonation = d.child("type").value.toString(),
                        amount = d.child("amount").value.toString().toIntOrNull() ?: 0,
                        timestamp = d.child("timestamp").value.toString().toLong(),
                        projectTitle = d.child("projectTitle").value.toString(),
                        associationName = d.child("associationName").value.toString()
                    )
                )
            }
        }
        return donations
    }

    // ==================== UTILITY METHODS ====================

    // 🔹 Vérifier si un donor existe
    suspend fun donorExists(uid: String): Boolean {
        return try {
            val snap = database.child("donors").child(uid).get().await()
            snap.exists()
        } catch (e: Exception) {
            false
        }
    }

    // 🔹 Supprimer un projet
    suspend fun deleteProject(projectId: String) {
        // Trouver le projet par son ID et le supprimer
        val projectsSnap = database.child("projects").get().await()
        for (p in projectsSnap.children) {
            if (p.child("id").value.toString() == projectId) {
                database.child("projects").child(p.key!!).removeValue().await()
                break
            }
        }
    }


}
