package project.controller.admin;

import project.dao.admin.MechanicDAO;
import project.model.Mechanic;

import com.google.cloud.firestore.Firestore;

import java.util.ArrayList;
import java.util.List;

public class MechanicController {

    private final MechanicDAO mechanicDAO;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public MechanicController(
            Firestore firestore
    ) {

        this.mechanicDAO =
                new MechanicDAO(firestore);
    }

    // =====================================================
    // GET ALL MECHANICS
    // =====================================================

    public List<Mechanic> getAllMechanics() {

        try {

            return mechanicDAO
                    .getAllMechanics();

        } catch (Exception e) {

            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    // =====================================================
    // GET MECHANIC BY ID
    // =====================================================

    public Mechanic getMechanicById(
            String mechanicId
    ) {

        try {

            return mechanicDAO
                    .getMechanicById(
                            mechanicId
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    // =====================================================
    // ADD MECHANIC
    // =====================================================

    public boolean addMechanic(
            Mechanic mechanic
    ) {

        try {

            if (
                    mechanic == null
            ) {

                return false;
            }

            // Basic validation
            if (
                    mechanic.getName() == null ||
                    mechanic.getName().isBlank()
            ) {

                return false;
            }

            if (
                    mechanic.getEmail() == null ||
                    mechanic.getEmail().isBlank()
            ) {

                return false;
            }

            if (
                    mechanic.getPhone() == null ||
                    mechanic.getPhone().isBlank()
            ) {

                return false;
            }

            if (
                    mechanic.getStatus() == null ||
                    mechanic.getStatus().isBlank()
            ) {

                mechanic.setStatus(
                        "Active"
                );
            }

            if (
                    mechanic.getCreatedAt() == null ||
                    mechanic.getCreatedAt().isBlank()
            ) {

                mechanic.setCreatedAt(
                        String.valueOf(
                                System.currentTimeMillis()
                        )
                );
            }

            return mechanicDAO
                    .addMechanic(
                            mechanic
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // UPDATE MECHANIC
    // =====================================================

    public boolean updateMechanic(
            Mechanic mechanic
    ) {

        try {

            if (
                    mechanic == null ||
                    mechanic.getMechanicId() == null ||
                    mechanic.getMechanicId().isBlank()
            ) {

                return false;
            }

            return mechanicDAO
                    .updateMechanic(
                            mechanic
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // DELETE MECHANIC
    // =====================================================

    public boolean deleteMechanic(
            String mechanicId
    ) {

        try {

            if (
                    mechanicId == null ||
                    mechanicId.isBlank()
            ) {

                return false;
            }

            return mechanicDAO
                    .deleteMechanic(
                            mechanicId
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // SEARCH MECHANICS
    // =====================================================

    public List<Mechanic> searchMechanics(
            String searchText
    ) {

        try {

            return mechanicDAO
                    .searchMechanics(
                            searchText
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    // =====================================================
    // FILTER BY STATUS
    // =====================================================

    public List<Mechanic> getMechanicsByStatus(
            String status
    ) {

        try {

            return mechanicDAO
                    .getMechanicsByStatus(
                            status
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    // =====================================================
    // STATISTICS
    // =====================================================

    public int getTotalMechanics() {

        return getAllMechanics()
                .size();
    }

    public int getActiveMechanics() {

        List<Mechanic> mechanics =
                getAllMechanics();

        int count = 0;

        for (
                Mechanic mechanic :
                mechanics
        ) {

            if (
                    mechanic.getStatus() != null &&
                    mechanic.getStatus()
                            .equalsIgnoreCase(
                                    "Active"
                            )
            ) {

                count++;
            }
        }

        return count;
    }

    public int getInactiveMechanics() {

        List<Mechanic> mechanics =
                getAllMechanics();

        int count = 0;

        for (
                Mechanic mechanic :
                mechanics
        ) {

            if (
                    mechanic.getStatus() == null ||
                    !mechanic.getStatus()
                            .equalsIgnoreCase(
                                    "Active"
                            )
            ) {

                count++;
            }
        }

        return count;
    }
}