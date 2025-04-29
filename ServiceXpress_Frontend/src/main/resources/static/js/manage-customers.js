$(document).ready(function () {
    const token = $('#token').val();
    const backendApiUrl = $('#backendApiUrl').val();

    // Initialize Active Customers Table
    $('#activeCustomersTable').DataTable({
        serverSide: true,
        ajax: {
            url: `${backendApiUrl}/customers/active`,
            headers: { Authorization: `Bearer ${token}` },
            data: function (d) {
                d.page = d.start / d.length;
                d.size = d.length;
                d.sort = d.columns[d.order[0].column].data + ',' + d.order[0].dir;
            },
            dataSrc: 'content'
        },
        columns: [
            { data: 'id' },
            { data: 'username' },
            { data: 'email' },
            { data: 'phoneNumber' },
            {
                data: null,
                render: function (data) {
                    return `
                        <a href="/customers/edit/${data.id}" class="btn btn-sm btn-primary">Edit</a>
                        <form action="/customers/deactivate/${data.id}" method="post" style="display:inline;">
                            <button type="submit" class="btn btn-sm btn-danger">Deactivate</button>
                        </form>
                    `;
                }
            }
        ],
        pageLength: 10,
        lengthMenu: [10, 25, 50],
        processing: true
    });

    // Initialize Inactive Customers Table
    $('#inactiveCustomersTable').DataTable({
        serverSide: true,
        ajax: {
            url: `${backendApiUrl}/customers/inactive`,
            headers: { Authorization: `Bearer ${token}` },
            data: function (d) {
                d.page = d.start / d.length;
                d.size = d.length;
                d.sort = d.columns[d.order[0].column].data + ',' + d.order[0].dir;
            },
            dataSrc: 'content'
        },
        columns: [
            { data: 'id' },
            { data: 'username' },
            { data: 'email' },
            { data: 'phoneNumber' },
            {
                data: null,
                render: function (data) {
                    return `
                        <form action="/customers/reactivate/${data.id}" method="post" style="display:inline;">
                            <button type="submit" class="btn btn-sm btn-success">Reactivate</button>
                        </form>
                    `;
                }
            }
        ],
        pageLength: 10,
        lengthMenu: [10, 25, 50],
        processing: true
    });
});