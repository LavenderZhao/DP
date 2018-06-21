SELECT
    A.id AS AID,
    A.content AS AContent,
    B.id AS BID
FROM
    A
INNER JOIN B ON (A.id = B.id)


