$(document).ready(function() {
    const token = $('#token').val();
    const backendApiUrl = $('#backendApiUrl').val();

    // Initialize Active Customers Table
    if ($('#activeCustomersTable').length) {
        $('#activeCustomersTable').DataTable({
            serverSide: true,
            ajax: {
                url: `${backendApiUrl}/customers/active`,
                headers: { Authorization: `Bearer ${token}` },
              
                dataSrc: 'content'
            },
            columns: [
                { data: 'id' },
                { data: 'username' },
                { data: 'email' },
                { data: 'phoneNumber' },
                {
                    data: null,
                    render: function(data) {
                        return `
                            <a href="/customers/edit/${data.id}" class="btn btn-sm btn-primary">Edit</a>
                            <form action="/customers/deactivate/${data.id}" method="post" style="display:inline;">
                                <button type="submit" class="btn btn-sm btn-danger">Deactivate</button>
                            </form>
                        `;
                    }
                }
            ],
            dom: 'Bfrtip',
            buttons: ['csv', 'excel'],
            pageLength: 5,
            autoWidth: false,
            ordering: true,
            searching: true,
            paging: true,
            pagingType: 'simple_numbers', // Use simple pagination style (Previous, 1, 2, Next)
            processing: true,
            columnDefs: [
                { targets: 0, visible: false } // Hide ID column explicitly
            ]
        });
    }

    // Initialize Inactive Customers Table
    if ($('#inactiveCustomersTable').length) {
        $('#inactiveCustomersTable').DataTable({
            serverSide: true,
            ajax: {
                url: `${backendApiUrl}/customers/inactive`,
                headers: { Authorization: `Bearer ${token}` },
             
                dataSrc: 'content'
            },
            columns: [
                { data: 'id' },
                { data: 'username' },
                { data: 'email' },
                { data: 'phoneNumber' },
                {
                    data: null,
                    render: function(data) {
                        return `
                            <form action="/customers/reactivate/${data.id}" method="post" style="display:inline;">
                                <button type="submit" class="btn btn-sm btn-success">Reactivate</button>
                            </form>
                        `;
                    }
                }
            ],
            dom: 'Bfrtip',
            buttons: ['csv', 'excel'],
            pageLength: 5,
            autoWidth: false,
            ordering: true,
            searching: true,
            paging: true,
            
            processing: true,
            columnDefs: [
                { targets: 0, visible: false } // Hide ID column explicitly
            ]
        });
    }
});