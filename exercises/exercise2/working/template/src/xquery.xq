(: Your XQuery goes here :)
<vaccinesByRiskGroups>
{
    let $vacPlan := doc("../resources/vaccination-plan-xsd.xml")/vaccination-plan
    for $vactype in $vacPlan/vaccine-types/vaccine
    let $vac := $vacPlan/vaccines/vaccine[@type_ref = $vactype/name]
    let $pat := $vacPlan/patients/patient[vaccine/@ref_batch = $vac/batch/@id]

    where count($pat) != 0
    order by $vac/name

    return
    <vaccine name="{$vactype/name}" type="{$vactype/type}">
        <patientsCount>{count($pat)}</patientsCount>
        {
            for $p in $pat[position()<3]
            return <patient riskGroup="{$p/risk-group/text()}">{$p/@name/string()}</patient>
        }
    </vaccine>
}
</vaccinesByRiskGroups>    