package dzcorp.jpa.persistence;

import dzcorp.jpa.model.Employee;
import dzcorp.jpa.model.EmployeePage;
import dzcorp.jpa.model.EmployeeSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class EmployeeGateway {

    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    private final EmployeeRepository employeeRepository;


    public EmployeeGateway(EntityManager entityManager, EmployeeRepository employeeRepository) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.employeeRepository = employeeRepository;
    }


    /**
     * return List<Employee>
     * Pageable = for setting page=page based on size if size is greater than page will increment,size = how many item will be appear ,
     * we can do sort by add param ASC/DESC , and sort by value
     * <p>
     * criteriaBuilder1.equal = is the same like sql = , and criteriaBuilder1.like is also the same 'like' in sql
     * <p>
     * //return predicate predicates.toArray(new Predicate[0])
     * criteriaBuilder1.and(predicates.toArray(new Predicate[0]));
     *
     * @param employeeSearchCriteria
     * @return
     */
    public List<Employee> getByFilter(EmployeeSearchCriteria employeeSearchCriteria) {
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
        Page<Employee> allByFilter = employeeRepository.findAll(getEmployeeWithSpecification(employeeSearchCriteria), pageable);
        return allByFilter.getContent();
    }


    private Specification<Employee> getEmployeeWithSpecification(EmployeeSearchCriteria searchCriteria) {
        return (root, query, criteriaBuilder1) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(searchCriteria.getFirstName())) {
                predicates.add(criteriaBuilder1.equal(root.get("firstName"), searchCriteria.getFirstName()));
            }

            if (Objects.nonNull(searchCriteria.getLastName())) {
                predicates.add(criteriaBuilder1.equal(root.get("lastName"), searchCriteria.getLastName()));
            }
            return criteriaBuilder1.and(predicates.toArray(new Predicate[0]));
        };
    }


    public List<Employee> filterByName(EmployeeSearchCriteria employeeSearchCriteria) {
        //we have to build the criteria query by using criteriaBuilder.createQuery
        CriteriaQuery<Employee> criteriaQuery = criteriaBuilder.createQuery(Employee.class);
        //to define root in model ex , firstName (name of the field)

        Root<Employee> employeeRoot = criteriaQuery.from(Employee.class);
        //predicates to filter value from request
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(employeeSearchCriteria.getFirstName())) {
            predicates.add(criteriaBuilder.equal(employeeRoot.get("firstName"), employeeSearchCriteria.getFirstName()));
        }

        if (Objects.nonNull(employeeSearchCriteria.getLastName())) {
            predicates.add(criteriaBuilder.like(employeeRoot.get("lastName"), employeeSearchCriteria.getLastName()));
        }

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        TypedQuery<Employee> typedQuery = entityManager.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    public Page<Employee> findAllWithFilters(EmployeePage employeePage, EmployeeSearchCriteria employeeSearchCriteria) {

        CriteriaQuery<Employee> criteriaQuery = criteriaBuilder.createQuery(Employee.class);
        Root<Employee> employeeRoot = criteriaQuery.from(Employee.class);

        Predicate predicate = getPredicate(employeeSearchCriteria, employeeRoot);

        criteriaQuery.where(predicate);
        criteriaQuery.orderBy(criteriaBuilder.desc(employeeRoot.get("firstName")));

        setOrder(employeePage, criteriaQuery, employeeRoot);

        TypedQuery<Employee> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(employeePage.getPageNumber() * employeePage.getPageSize());
        typedQuery.setMaxResults(employeePage.getPageSize());

        Pageable pageable = getPageable(employeePage);

        long employeesCount = getEmployeesCount(predicate);


        return new PageImpl<>(typedQuery.getResultList(), pageable, employeesCount);
    }


    private long getEmployeesCount(Predicate predicate) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Employee> countRoot = countQuery.from(Employee.class);
        countQuery.select(criteriaBuilder.count(countRoot)).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Pageable getPageable(EmployeePage employeePage) {
        Sort sort = Sort.by(employeePage.getSortDirection(), employeePage.getSortBy());
        return PageRequest.of(employeePage.getPageNumber(), employeePage.getPageSize(), sort);
    }

    private void setOrder(EmployeePage employeePage, CriteriaQuery<Employee> criteriaQuery, Root<Employee> employeeRoot) {
        if (employeePage.getSortDirection().equals(Sort.Direction.ASC)) {
            criteriaQuery.orderBy(criteriaBuilder.asc(employeeRoot.get(employeePage.getSortBy())));
        } else {
            criteriaQuery.orderBy(criteriaBuilder.desc(employeeRoot.get(employeePage.getSortBy())));
        }
    }


    private Predicate getPredicate(EmployeeSearchCriteria employeeSearchCriteria, Root<Employee> employeeRoot) {
        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(employeeSearchCriteria.getFirstName())) {
            predicates.add(criteriaBuilder.like(employeeRoot.get("firstName"), "%" + employeeSearchCriteria.getFirstName() + "%"));
        }

        if (Objects.nonNull(employeeSearchCriteria.getLastName())) {
            predicates.add(criteriaBuilder.like(employeeRoot.get("lastName"), "%" + employeeSearchCriteria.getLastName() + "%"));
        }


        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }


}
